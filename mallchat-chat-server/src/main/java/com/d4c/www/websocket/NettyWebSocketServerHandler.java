package com.d4c.www.websocket;


import cn.hutool.extra.spring.SpringUtil;
import cn.hutool.json.JSONUtil;
import com.d4c.www.websocket.domain.enums.WSReqTypeEnum;
import com.d4c.www.websocket.domain.vo.req.ws.WSBaseReq;
import com.d4c.www.websocket.service.WebSocketService;
import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketServerProtocolHandler;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import lombok.extern.slf4j.Slf4j;


@Slf4j
@Sharable
public class NettyWebSocketServerHandler extends SimpleChannelInboundHandler<TextWebSocketFrame> {

    private WebSocketService webSocketService;



    /**
     * web客户端连接后，触发该方法，将channel保存起来
     * @param ctx
     * @throws Exception
     */
    @Override
    public void handlerAdded(ChannelHandlerContext ctx) throws Exception {
        //获取bean
        this.webSocketService = SpringUtil.getBean(WebSocketService.class);
        //保存channel的具体实现逻辑
        webSocketService.connect(ctx.channel());
    }
    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        if(evt instanceof WebSocketServerProtocolHandler.HandshakeComplete){
             log.info("用户连接成功");
        } else if(evt instanceof IdleStateEvent){
            IdleStateEvent event = (IdleStateEvent) evt;
            if( event.state() == IdleState.READER_IDLE){
                log.info("读超时");
            }
        }
    }


    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, TextWebSocketFrame textWebSocketFrame) throws Exception {
        WSBaseReq wsBaseReq = JSONUtil.toBean(textWebSocketFrame.text(), WSBaseReq.class);
        WSReqTypeEnum wsReqTypeEnum = WSReqTypeEnum.of(wsBaseReq.getType());
        switch (wsReqTypeEnum) {
            case LOGIN:
                this.webSocketService.handleLoginReq(channelHandlerContext.channel());
                log.info("请求二维码 = " + textWebSocketFrame.text());
                break;
            case HEARTBEAT:
                break;
            default:
                log.info("未知类型");
        }
    }
}
