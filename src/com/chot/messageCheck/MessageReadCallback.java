package com.chot.messageCheck;

import com.tibco.tibrv.*;

/**
 * 消息监听
 */
public interface MessageReadCallback extends TibrvMsgCallback, TibrvTimerCallback,
        TibrvFtMemberCallback, TibrvFtMonitorCallback {
    /**
     * 接收消息并处理
     *
     * @param tibrvMsg
     */
    @Override
    void onMsg(TibrvListener tibrvListener, TibrvMsg tibrvMsg);

    /**
     * 主备切换
     *
     * @param member
     * @param groupName
     * @param action
     */
    @Override
    void onFtAction(TibrvFtMember member, String groupName, int action);


    /**
     * 监听RV挂掉
     *
     * @param ftMonitor
     * @param ftgroupName
     * @param numActive
     */
    @Override
    void onFtMonitor(TibrvFtMonitor ftMonitor, String ftgroupName, int numActive);

    /**
     * 计时器
     *
     * @param tibrvTimer
     */
    @Override
    void onTimer(TibrvTimer tibrvTimer);
}
