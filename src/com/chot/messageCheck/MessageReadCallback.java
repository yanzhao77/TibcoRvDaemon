package com.chot.messageCheck;

import com.tibco.tibrv.TibrvMsg;

/**
 * 消息监听
 */
public interface MessageReadCallback {

    /**
     * 接收消息并处理
     *
     * @param msg
     */
    void readMessage(TibrvMsg msg);
}
