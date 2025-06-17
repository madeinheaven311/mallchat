package com.d4c.www.user.service.adapter;

import cn.hutool.core.bean.BeanUtil;

import com.d4c.www.websocket.domain.enums.WSRespTypeEnum;
import com.d4c.www.websocket.domain.vo.resp.ws.WSBaseResp;
import com.d4c.www.websocket.domain.vo.resp.ws.WSLoginUrl;
import me.chanjar.weixin.mp.bean.result.WxMpQrCodeTicket;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Collections;

/**
 * Description: ws消息适配器
 * Author: <a href="https://github.com/zongzibinbin">abin</a>
 * Date: 2023-03-19
 */
@Component
public class WSAdapter {


    public static WSBaseResp<WSLoginUrl> buildLoginResp(WxMpQrCodeTicket wxMpQrCodeTicket) {
        WSBaseResp<WSLoginUrl> wsBaseResp = new WSBaseResp<>();
        wsBaseResp.setType(WSRespTypeEnum.LOGIN_URL.getType());
        wsBaseResp.setData(WSLoginUrl.builder().loginUrl(wxMpQrCodeTicket.getUrl()).build());
        return wsBaseResp;
    }


}
