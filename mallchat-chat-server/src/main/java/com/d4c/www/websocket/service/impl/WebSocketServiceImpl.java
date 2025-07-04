package com.d4c.www.websocket.service.impl;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.json.JSONUtil;
import com.d4c.www.common.constant.RedisKey;
import com.d4c.www.common.util.RedisUtils;
import com.d4c.www.user.dao.UserDao;
import com.d4c.www.user.domain.entity.User;
import com.d4c.www.user.domain.enums.RoleEnum;
import com.d4c.www.user.service.IRoleService;
import com.d4c.www.user.service.LoginService;
import com.d4c.www.user.service.adapter.WSAdapter;
import com.d4c.www.websocket.domain.dto.WSChannelExtraDTO;
import com.d4c.www.websocket.domain.vo.req.ws.WSAuthorize;
import com.d4c.www.websocket.domain.vo.resp.ws.WSBaseResp;
import com.d4c.www.websocket.service.WebSocketService;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import io.netty.channel.Channel;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import lombok.SneakyThrows;
import me.chanjar.weixin.mp.api.WxMpService;
import me.chanjar.weixin.mp.bean.result.WxMpQrCodeTicket;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.TimeUnit;

public class WebSocketServiceImpl implements WebSocketService {

    private static final Duration EXPIRE_TIME = Duration.ofHours(1);
    private static final Long MAX_MUM_SIZE = 10000L;
    /**
     * 所有请求登录的code与channel关系
     */
    public static final Cache<Integer, Channel> WAIT_LOGIN_MAP = Caffeine.newBuilder()
            .expireAfterWrite(EXPIRE_TIME)
            .maximumSize(MAX_MUM_SIZE)
            .build();
    /**
     * 所有已连接的websocket连接列表和一些额外参数
     */
    private static final ConcurrentHashMap<Channel, WSChannelExtraDTO> ONLINE_WS_MAP = new ConcurrentHashMap<>();
    /**
     * 所有在线的用户和对应的socket
     */
    private static final ConcurrentHashMap<Long, CopyOnWriteArrayList<Channel>> ONLINE_UID_MAP = new ConcurrentHashMap<>();


    public static ConcurrentHashMap<Channel, WSChannelExtraDTO> getOnlineMap() {
        return ONLINE_WS_MAP;
    }
    private static final String LOGIN_CODE = "loginCode";
    @Autowired
    private WxMpService wxMpService;
    @Autowired
    private UserDao userDao;
    @Autowired
    private LoginService loginService;
    @Autowired
    private IRoleService iRoleService;
    @Override
    public void connect(Channel channel) {
        ONLINE_WS_MAP.put(channel, new WSChannelExtraDTO());
    }

    @SneakyThrows
    @Override
    public void handleLoginReq(Channel channel)  {
        //生成随机不重复的登录码,并将channel存在本地cache中
        Integer code = generateLoginCode(channel);
        //请求微信接口，获取登录码地址
        WxMpQrCodeTicket wxMpQrCodeTicket = wxMpService.getQrcodeService().qrCodeCreateTmpTicket(code, (int) EXPIRE_TIME.getSeconds());
        //返回给前端（channel必在本地）
        sendMsg(channel, WSAdapter.buildLoginResp(wxMpQrCodeTicket));
    }

    @Override
    public void removed(Channel channel) {
        WSChannelExtraDTO wsChannelExtraDTO = ONLINE_WS_MAP.get(channel);
        Optional<Long> uidOptional = Optional.ofNullable(wsChannelExtraDTO)
                .map(WSChannelExtraDTO::getUid);
        boolean offlineAll = offline(channel, uidOptional);
        if (uidOptional.isPresent() && offlineAll) {//已登录用户断连,并且全下线成功
            User user = new User();
            user.setId(uidOptional.get());
            user.setLastOptTime(LocalDateTime.now());
            //applicationEventPublisher.publishEvent(new UserOfflineEvent(this, user));
        }
    }

    /**
     * 获取不重复的登录的code，微信要求最大不超过int的存储极限
     * 防止并发，可以给方法加上synchronize，也可以使用cas乐观锁
     *
     * @return
     */
    private Integer generateLoginCode(Channel channel) {
        int inc;
        do {
            //本地cache时间必须比redis key过期时间短，否则会出现并发问题
            inc = RedisUtils.integerInc(RedisKey.getKey(LOGIN_CODE), (int) EXPIRE_TIME.toMinutes(), TimeUnit.MINUTES);
        } while (WAIT_LOGIN_MAP.asMap().containsKey(inc));
        //储存一份在本地
        WAIT_LOGIN_MAP.put(inc, channel);
        return inc;
    }

    /**
     * 用户下线
     * return 是否全下线成功
     */
    private boolean offline(Channel channel, Optional<Long> uidOptional) {
        ONLINE_WS_MAP.remove(channel);
        if (uidOptional.isPresent()) {
            CopyOnWriteArrayList<Channel> channels = ONLINE_UID_MAP.get(uidOptional.get());
            if (CollectionUtil.isNotEmpty(channels)) {
                channels.removeIf(ch -> Objects.equals(ch, channel));
            }
            return CollectionUtil.isEmpty(ONLINE_UID_MAP.get(uidOptional.get()));
        }
        return true;
    }

    /**
     * 给本地channel发送消息
     *
     * @param channel
     * @param wsBaseResp
     */
    private void sendMsg(Channel channel, WSBaseResp<?> wsBaseResp) {
        channel.writeAndFlush(new TextWebSocketFrame(JSONUtil.toJsonStr(wsBaseResp)));
    }

    /**
     * 扫码登录成功，需要将用户和连接进行处理
     * @param loginCode
     * @param uid
     * @return
     */
    @Override
    public Boolean scanLoginSuccess(Integer loginCode, Long uid) {
        //确认连接在该机器
        Channel channel = WAIT_LOGIN_MAP.getIfPresent(loginCode);
        if (Objects.isNull(channel)) {
            return Boolean.FALSE;
        }
        User user = userDao.getById(uid);
        //移除code
        WAIT_LOGIN_MAP.invalidate(loginCode);
        //调用用户登录模块
        String token = loginService.login(uid);
//        //用户登录
        loginSuccess(channel, user, token);
        return Boolean.TRUE;
    }

    @Override
    public void authorize(Channel channel, WSAuthorize wsAuthorize) {
        //校验token
        boolean verifySuccess = loginService.verify(wsAuthorize.getToken());
        if (verifySuccess) {//用户校验成功给用户登录
            User user = userDao.getById(loginService.getValidUid(wsAuthorize.getToken()));
            loginSuccess(channel, user, wsAuthorize.getToken());
        } else { //让前端的token失效
            sendMsg(channel, WSAdapter.buildInvalidateTokenResp());
        }
    }

    /**
     * (channel必在本地)登录成功，并更新状态
     */
    private void loginSuccess(Channel channel, User user, String token) {
        //更新上线列表
        online(channel, user.getId());
        //返回给用户登录成功
        boolean hasPower = iRoleService.hasPower(user.getId(), RoleEnum.CHAT_MANAGER);
        //发送给对应的用户
        sendMsg(channel, WSAdapter.buildLoginSuccessResp(user, token, hasPower));
        //发送用户上线事件
        //boolean online = userCache.isOnline(user.getId());
        if (1==1) {
//            user.setLastOptTime(new Date());
//            user.refreshIp(NettyUtil.getAttr(channel, NettyUtil.IP));
//            applicationEventPublisher.publishEvent(new UserOnlineEvent(this, user));
        }
    }

    /**
     * 用户上线
     */
    private void online(Channel channel, Long uid) {
//        getOrInitChannelExt(channel).setUid(uid);
//        ONLINE_UID_MAP.putIfAbsent(uid, new CopyOnWriteArrayList<>());
//        ONLINE_UID_MAP.get(uid).add(channel);
//        NettyUtil.setAttr(channel, NettyUtil.UID, uid);
    }

}
