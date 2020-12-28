package com.chot.messageCheck;

import com.tibco.tibrv.TibrvListener;
import com.tibco.tibrv.TibrvMsg;
import com.tibco.tibrv.TibrvMsgCallback;

/**
 * 消息监听
 */
public interface MessageReadCallback extends TibrvMsgCallback {
    /**
     * 接收消息并处理
     *
     * @param tibrvMsg
     */
    @Override
    void onMsg(TibrvListener tibrvListener, TibrvMsg tibrvMsg);
}
