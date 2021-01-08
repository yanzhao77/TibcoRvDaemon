package com.chot.rvLister;

import com.chot.entity.daesonEntity.TibrvRvdTransportParameter;
import com.tibco.tibrv.*;
import org.apache.log4j.Logger;

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

    //需要进行主备切换消息
    static String[] switchServiceParameter = new String[]{
            "_RV.INFO.SYSTEM.LISTEN.STOP", //监听停止
            "_RV.INFO.SYSTEM.UNREACHABLE.TRANSPORT", //消息发送停止
            "_RV.WARN.SYSTEM.RVD.DISCONNECTED", //服务器daemon断开连接
//            "_RV.INFO.SYSTEM.LISTEN.START", //未启动这个频道？监听这个频道
    };
    static String[] refreshServiceParameterMap = new String[]{//需要刷新链接组信息
            "_RV.INFO.SYSTEM.RVD.CONNECTED", //服务器daemon重新连接
            "_RV.WARN.SYSTEM.RVD.DISCONNECTED", //服务器daemon断开连接
    };

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
     * 停止这个监听，启动备用监听
     *
     * @param tibrvTimerTransport 异常的监听
     */
    public void resertTibrvRvdListener(TibrvRvdTransport tibrvTimerTransport) throws TibrvException {
        TibrvRvdTransportParameter transportParameter = findTibrvRvdTransportByParameter(tibrvTimerTransport);
        tibrvTimerTransport = transportParameter.getTibrvRvdTransport();
        //计算有效的可用的备用机数量
        List<TibrvRvdTransportParameter> transportParameterValidityList = new ArrayList<>();
        rvListener.getTransportGroup().get(transportParameter.getGroupName()).forEach(transportfe -> {
            if (transportfe.isValidityFlag()) transportParameterValidityList.add(transportfe);
        });
        for (TibrvRvdTransportParameter tibrvRvdTransportParameter : transportParameterValidityList) {
            if (tibrvRvdTransportParameter != transportParameter &
                    !tibrvRvdTransportParameter.isStartListener()) {
                TibrvRvdTransport rvdTransport = null;
                try {
                    rvdTransport = tibrvRvdTransportParameter.getTibrvRvdTransport();
                } catch (TibrvException e) {
                    transportParameter.setValidityFlag(false);
                    logger.error("Failed to create TibrvQueue:" + e.getLocalizedMessage(), e.getCause());
                }
                logger.debug("TibrvFtMember.ACTIVATE invoked...立即激活" + "\t" + rvdTransport + "\n"
                        + "*** ACTIVATE: " + Arrays.toString(tibrvRvdTransportParameter.getSubject()));
                startListener(rvdTransport, tibrvRvdTransportParameter.getGroupName());
                stopListener(rvdTransport, tibrvRvdTransportParameter.getGroupName());
                rvListener.setTransport(rvdTransport);
            }
        }


    }

    /**
     * 处理异常
     *
     * @param o
     * @param i
     * @param s
     * @param throwable
     */
    public void onError(Object o, int i, String s, Throwable throwable) {
        System.err.println("onError" + o + "\t" + i + "\t" + s);
        logger.error(o + "\t" + i + "\t" + s + "\t" + throwable.getLocalizedMessage());
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
                if (tibrvRvdTransportParameter.getTibrvRvdTransport() == transport &
                        tibrvRvdTransportParameter.isValidityFlag()) {
                    for (String subject : tibrvRvdTransportParameter.getSubject()) {
                        rvListener.setTibrvListener(transport, Tibrv.defaultQueue(), subject, tibrvRvdTransportParameter);
                        System.out.println("Start Listening on: " + subject);
                    }
                    tibrvRvdTransportParameter.setStartListener(true);
                    tibrvRvdTransportParameter.setValidityFlag(true);
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
                        List<TibrvListener> tibrvListeners = tibrvRvdTransportParameter.getTibrvListenerMap().get(tibrvRvdTransport);
                        tibrvListeners.forEach(tibrvListener -> {
                            //除异常监听外，全部关闭
                            if (!tibrvListener.getSubject().equals("_RV.>")) {
                                tibrvListener.destroy();
                                System.out.println("Destroy Listener on Subject: " + tibrvListener.getSubject());
                            }
                        });
                    }
                    tibrvRvdTransportParameter.setStartListener(false);
                }
            }
        }
    }


    /**
     * 处理异常消息
     *
     * @param tibrvListener
     * @param tibrvMsg
     * @throws TibrvException
     */
    public void warnAndErrorCheckForMessage(TibrvListener tibrvListener, TibrvMsg tibrvMsg) throws TibrvException {

        TibrvRvdTransportParameter rvdTransportParameter = findTibrvRvdTransportByParameter((TibrvRvdTransport) tibrvListener.getTransport());
        TibrvRvdTransport transport = rvdTransportParameter.getTibrvRvdTransport();
        String subjectName = tibrvMsg.getSendSubject();
        if (subjectName.contains("_RV.INFO.SYSTEM.HOST.STATUS")) {
            //检测远程daemon是否正常
            logger.debug(transport + "\t" + tibrvListener.getSubject() + "\t"
                    + "\t" + subjectName + "\t" + tibrvMsg.toString());
            return;
        } else if (subjectName.contains("_RV.INFO.SYSTEM.RVD.CONNECTED")) {//服务器daemon重新连接
            refreshTibrvRvdTransportMap(transport, true);
        } else {
            logger.error(transport + "\t" + tibrvListener.getSubject() + "\t"
                    + "\t" + subjectName + "\t" + tibrvMsg.toString());
            if (subjectName.contains("_RV.WARN.SYSTEM.RVD.DISCONNECTED")) {//服务器daemon断开连接
                refreshTibrvRvdTransportMap(transport, false);
            }
            resertTibrvRvdListener(transport);
        }
    }

    /**
     * 刷新这个组成员
     *
     * @param transport
     * @param validityFlag
     */
    void refreshTibrvRvdTransportMap(TibrvRvdTransport transport, boolean validityFlag) {
        TibrvRvdTransportParameter tibrvRvdTransportByParameter = findTibrvRvdTransportByParameter(transport);
        tibrvRvdTransportByParameter.setValidityFlag(validityFlag);
    }

    /**
     * 查找对应的 TibrvRvdTransportParameter
     *
     * @param transport
     * @return
     */
    TibrvRvdTransportParameter findTibrvRvdTransportByParameter(TibrvRvdTransport transport) {
        for (List<TibrvRvdTransportParameter> rvdTransportParameterList : rvListener.getTransportGroup().values()) {
            for (TibrvRvdTransportParameter transportParameter : rvdTransportParameterList) {
                if (transportParameter.getService().equals(transport.getService()) &
                        transportParameter.getNetwork().equals(transport.getNetwork()) &
                        transportParameter.getDaemon().equals(transport.getDaemon())) {
                    return transportParameter;
                }
            }
        }
        return null;
    }
}
