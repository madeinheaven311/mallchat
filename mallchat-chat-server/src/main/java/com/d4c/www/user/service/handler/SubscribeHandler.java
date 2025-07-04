package com.d4c.www.user.service.handler;


import me.chanjar.weixin.common.error.WxErrorException;
import me.chanjar.weixin.common.session.WxSessionManager;
import me.chanjar.weixin.mp.api.WxMpService;
import me.chanjar.weixin.mp.bean.message.WxMpXmlMessage;
import me.chanjar.weixin.mp.bean.message.WxMpXmlOutMessage;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * @author <a href="https://github.com/binarywang">Binary Wang</a>
 */
@Component
public class SubscribeHandler extends AbstractHandler {
//    @Autowired
//    private WxMsgService wxMsgService;

    @Override
    public WxMpXmlOutMessage handle(WxMpXmlMessage wxMessage,
                                    Map<String, Object> context, WxMpService weixinService,
                                    WxSessionManager sessionManager) throws WxErrorException {

        this.logger.info("新关注用户 OPENID: " + wxMessage.getFromUser());

//        WxMpXmlOutMessage responseResult = null;
//        try {
//            responseResult = this.handleSpecial(weixinService, wxMessage);
//        } catch (Exception e) {
//            this.logger.error(e.getMessage(), e);
//        }
//
//        if (responseResult != null) {
//            return responseResult;
//        }
//
//        try {
//            return new TextBuilder().build("感谢关注", wxMessage, weixinService);
//        } catch (Exception e) {
//            this.logger.error(e.getMessage(), e);
//        }

        return null;
    }



}
