package com.d4c.www.websocket.service;

import com.d4c.www.websocket.domain.vo.req.ws.WSAuthorize;
import io.netty.channel.Channel;
import me.chanjar.weixin.common.error.WxErrorException;

public interface WebSocketService {

    /**
     * 处理所有ws连接的事件
     *
     * @param channel
     */
    void connect(Channel channel);

    void handleLoginReq(Channel channel) throws WxErrorException;

    void removed(Channel channel);

    Boolean scanLoginSuccess(Integer loginCode, Long uid);

    /**
     * 主动认证登录
     *
     * @param channel
     * @param wsAuthorize
     */
    void authorize(Channel channel, WSAuthorize wsAuthorize);
}
