package com.chot.rvLister;

import com.chot.entity.daesonEntity.TibrvRvdTransportParameter;
import com.tibco.tibrv.*;
import org.apache.log4j.Logger;
import sun.plugin2.message.transport.Transport;

import javax.xml.crypto.Data;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;

/**
 * @version 1.0
 * @Classname TibrvFtService
 * @Description TODO
 * @Date 2021/1/7 10:13
 * @Created by yan34177
 */
public class TibrvFtService {
    RvListener rvListener;
    Logger logger;
    static int oldNumActive = 0;


    public TibrvFtService(RvListener rvListener, Logger logger) {
        this.rvListener = rvListener;
        this.logger = logger;
    }

    public RvListener getRvListener() {
        return rvListener;
    }

    public void setRvListener(RvListener rvListener) {
        this.rvListener = rvListener;
    }

    /**
     * 保存接收消息的时间
     *
     * @param tibrvListener
     */
    public void setTimerIntervalMessageTime(TibrvListener tibrvListener) throws TibrvException {
        for (TibrvRvdTransportParameter tibrvTimer : rvListener.getTimerIntervalMessageMap().keySet()) {
            if (tibrvTimer.getTibrvRvdTransport() == tibrvListener.getTransport()) {
                //获取当前时间并写入
                rvListener.setTimerIntervalMessageMap(tibrvTimer, new Date().getTime());
            }
        }
    }

    public void onTimer(TibrvTimer tibrvTimer) throws TibrvException {
        //每10秒查看一次是否有接收到消息，如果没有，就切换到备用件
        TibrvRvdTransport tibrvTimerTransport = null;
        TibrvRvdTransportParameter transportParameter = null;
        long used = 0;
        for (TibrvRvdTransportParameter tibrvRvdTransportParameter : rvListener.getTimerIntervalMessageMap().keySet()) {
            if (tibrvTimer.getClosure() == tibrvRvdTransportParameter.getTibrvRvdTransport()) {
                transportParameter = tibrvRvdTransportParameter;
                tibrvTimerTransport = (TibrvRvdTransport) tibrvTimer.getClosure();
                long messageTime = rvListener.getTimerIntervalMessageMap().get(tibrvRvdTransportParameter);
                used = (new Date().getTime() - messageTime) / 1000;//获取时间差
            }
        }
        if (used < tibrvTimer.getInterval()) {
            rvListener.setTimerIntervalMessageMap(transportParameter, new Date().getTime());
        } else {
            if (rvListener.getTransport() == tibrvTimerTransport) {
                //切换监听
                List<TibrvRvdTransportParameter> transportParameterList = rvListener.getTransportGroup().get(transportParameter.getGroupName());
                for (TibrvRvdTransportParameter tibrvRvdTransportParameter : transportParameterList) {
                    if (tibrvRvdTransportParameter != transportParameter) {
                        TibrvRvdTransport rvdTransport = tibrvRvdTransportParameter.getTibrvRvdTransport();
                        startListener(rvdTransport, tibrvRvdTransportParameter.getGroupName());
                        stopListener(rvdTransport, tibrvRvdTransportParameter.getGroupName());
                        rvListener.setTransport(rvdTransport);
                        rvListener.setTimerIntervalMessageMap(tibrvRvdTransportParameter, new Date().getTime());
                    }
                }
            }
        }
    }

    /**
     * 活动数量监控
     *
     * @param member
     * @param groupName
     * @param action
     */
    @Deprecated
    public void onFtAction(TibrvFtMember member, String groupName, int action) {
        System.err.println(member);
//        if (action == TibrvFtMember.PREPARE_TO_ACTIVATE) {
//            //准备激活
//            System.err.println("TibrvFtMember.PREPARE_TO_ACTIVATE invoked...准备激活");
//            System.out.println("*** PREPARE TO ACTIVATE: " + member.getTransport());
//        } else if (action == TibrvFtMember.ACTIVATE) {
//            //立即激活
//            System.err.println("TibrvFtMember.ACTIVATE invoked...立即激活");
//            System.out.println("*** ACTIVATE: " + groupName);
//            try {
//                startListener(member.getTransport(), member.getGroupName());
//            } catch (TibrvException e) {
//                logger.error(e.getLocalizedMessage());
//            }
//            //立即停用
//            System.err.println("TibrvFtMember.DEACTIVATE invoked...立即停用");
//            try {
//                stopListener(member.getTransport(), member.getGroupName());
//            } catch (TibrvException e) {
//                logger.error(e.getLocalizedMessage());
//            }
//        } else if (action == TibrvFtMember.DEACTIVATE) {
//            //立即停用
//            System.err.println("TibrvFtMember.DEACTIVATE invoked...立即停用");
////                    System.out.println("*** DEACTIVATE: " + groupName);
//            try {
//                stopListener(member.getTransport(), member.getGroupName());
//            } catch (TibrvException e) {
//                logger.error(e.getLocalizedMessage());
//            }
//        }
    }

    /**
     * 监听RV活动状态
     *
     * @param ftMonitor
     * @param groupName
     * @param numActive
     */
    @Deprecated
    public void onFtMonitor(TibrvFtMonitor ftMonitor, String groupName, int numActive) {
        //如果有活动成员消息，就进行处理
        System.out.println(ftMonitor);
        System.err.println("Group [" + groupName + "]: has " + numActive + " members (after " +
                ((oldNumActive > numActive) ? "one deactivated" : "one activated") + ").");
        oldNumActive = numActive;
    }


    /**
     * 开启监听
     *
     * @param transport
     * @param groupName
     * @throws TibrvException
     */
    public void startListener(TibrvTransport transport, String groupName) throws TibrvException {
        List<TibrvRvdTransportParameter> rvdTransportParameterList = rvListener.getTransportGroup().get(groupName);
        if (rvdTransportParameterList != null) {
            for (TibrvRvdTransportParameter tibrvRvdTransportParameter : rvdTransportParameterList) {
                if (tibrvRvdTransportParameter.getTibrvRvdTransport() == transport) {
                    for (String subject : tibrvRvdTransportParameter.getSubject()) {
                        TibrvListener tibrvListener = new TibrvListener(Tibrv.defaultQueue(),
                                rvListener.getMessageRead(), transport, subject, null);
                        tibrvRvdTransportParameter.setTibrvListenerMap((TibrvRvdTransport) transport, tibrvListener);
                        System.out.println("Start Listening on: " + subject);
                    }
                }
            }
        }
    }

    /**
     * 停止监听
     *
     * @param transport
     * @param groupName
     * @throws TibrvException
     */
    public void stopListener(TibrvTransport transport, String groupName) throws TibrvException {
        List<TibrvRvdTransportParameter> rvdTransportParameterList = rvListener.getTransportGroup().get(groupName);
        if (rvdTransportParameterList != null) {
            for (TibrvRvdTransportParameter tibrvRvdTransportParameter : rvdTransportParameterList) {
                if (tibrvRvdTransportParameter.getTibrvRvdTransport() != transport) {
                    for (TibrvRvdTransport tibrvRvdTransport : tibrvRvdTransportParameter.getTibrvListenerMap().keySet()) {
                        if (tibrvRvdTransport != transport) {
                            List<TibrvListener> tibrvListeners = tibrvRvdTransportParameter.getTibrvListenerMap().get(tibrvRvdTransport);
                            tibrvListeners.forEach(tibrvListener -> {
                                tibrvListener.destroy();
                                System.out.println("Destroy Listener on Subject: " + tibrvListener.getSubject());
                            });
                        }
                    }
                }
            }
        }
    }

}
