package com.chot.messageCheck;

import com.tibco.tibrv.*;

/**
 * 消息监听
 */
public interface MessageReadCallback extends TibrvMsgCallback, TibrvErrorCallback {
    /**
     * 接收消息并处理
     *
     * @param tibrvMsg
     */
    @Override
    void onMsg(TibrvListener tibrvListener, TibrvMsg tibrvMsg);

    /**
     * 异常处理
     *
     * @param o
     * @param i
     * @param s
     * @param throwable
     */
    @Override
    void onError(Object o, int i, String s, Throwable throwable);

}
