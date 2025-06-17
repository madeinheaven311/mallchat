package com.d4c.www.websocket.service;

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
}
