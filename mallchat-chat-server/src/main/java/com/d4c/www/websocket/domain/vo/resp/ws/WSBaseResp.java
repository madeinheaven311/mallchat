package com.d4c.www.websocket.domain.vo.resp.ws;

import lombok.Data;

@Data
public class WSBaseResp<T> {
    /**
     * ws推送给前端的消息
     *
     * @see com.d4c.www.websocket.domain.enums.WSRespTypeEnum
     */
    private Integer type;
    private T data;
}
