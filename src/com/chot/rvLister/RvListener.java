package com.chot.rvLister;

import com.chot.messageCheck.MessageReadCallback;
import com.chot.entity.daesonEntity.TibrvRvdTransportParameter;
import com.chot.utils.LoggerUtil;
import com.tibco.tibrv.*;
import org.apache.log4j.Logger;

import java.sql.Time;
import java.time.Instant;
import java.util.*;

public class RvListener {
    MessageReadCallback messageRead;//统一的消息处理
    Logger logger;
    Map<String, List<TibrvRvdTransportParameter>> transportGroup;//多个server
    Map<TibrvRvdTransportParameter, Long> timerIntervalMessageMap;//如果这个timr在指定时间内没有收到消息，那么就切换监听，
    TibrvTransport transport;//当前正在运行的transport,(主备切换)
    public long stoptime = 60;//如果一分钟之内，主备机组都启动不了，就退出程序

    public RvListener() {
        logger = LoggerUtil.getLogger();
        // open Tibrv in native implementation
        try {
            Tibrv.open(Tibrv.IMPL_NATIVE);
        } catch (TibrvException e) {
            logger.error("Failed to open Tibrv in native implementation:" + e.getLocalizedMessage(), e.getCause());
            System.exit(0);
        }
    }


    /**
     * 启动监听
     */
    public void start() {
        TibrvQueue tibrvQueue = Tibrv.defaultQueue();
        if (messageRead == null) {
            messageRead = new MessageReadCallback() {
                @Override
                public void onMsg(TibrvListener tibrvListener, TibrvMsg tibrvMsg) {
                    RvListener.this.onMsg(tibrvListener, tibrvMsg);
                }

                @Override
                public void onFtAction(TibrvFtMember member, String groupName, int action) {
                    RvListener.this.onFtAction(member, groupName, action);
                }

                @Override
                public void onFtMonitor(TibrvFtMonitor ftMonitor, String ftgroupName, int numActive) {
                    RvListener.this.onFtMonitor(ftMonitor, ftgroupName, numActive);
                }

                @Override
                public void onTimer(TibrvTimer tibrvTimer) {
                    RvListener.this.onTimer(tibrvTimer);
                }
            };
        }
        boolean isStartTransport = false;//判断是否启动成功
        boolean isFirstFlag = true;//判断是否是首次启动
        for (String key : transportGroup.keySet()) {
            isStartTransport = false;
            List<TibrvRvdTransportParameter> transportParameterList = new ArrayList<>();
            transportParameterList.addAll(transportGroup.get(key));
            //如果key是默认机组，那么启动所有的Transport，如果不是，就只启动一个
            for (TibrvRvdTransportParameter transportParameter : transportParameterList) {
                // Create RVD transport
                TibrvTransport transport = null;
                try {
                    transport = transportParameter.getTibrvRvdTransport();
                    //启动一个定时器监听是否消息继续传输,设定5秒
                    try {
                        TibrvTimer fttimer = new TibrvTimer(Tibrv.defaultQueue(), messageRead, 5.0, transport);
                        getTimerIntervalMessageMap().put(transportParameter, new Date().getTime());
                    } catch (TibrvException e) {
                        logger.error("Failed to create timer:" + e.getLocalizedMessage());
                        System.exit(0);
                    }

                    if (!key.equals("default")) {
                        new TibrvFtMonitor(Tibrv.defaultQueue(), // TibrvQueue
                                messageRead,                 // TibrvFtMemberCallback
                                transportParameter.getTibrvRvdTransport(),          // TibrvTransport
                                transportParameter.getGroupName(),
                                transportParameter.getLostInterval(), null);
                        if (!isFirstFlag) {
                            new TibrvFtMember(Tibrv.defaultQueue(), // TibrvQueue
                                    messageRead,                 // TibrvFtMemberCallback
                                    transportParameter.getTibrvRvdTransport(),          // TibrvTransport
                                    transportParameter.getGroupName(),          // groupName 组名称
                                    transportParameter.getFtWeight(),             // 权重
                                    transportParameter.getActiveGoalNum(),        // activeGoal
                                    transportParameter.getHbInterval(),           // 心跳时间间隔
                                    transportParameter.getPrepareInterval(),      // 准备时间间隔,
                                    // Zero is a special value,零是一个特殊值
                                    // indicating that the member does 指示成员不需要预先警告即可激活
                                    transportParameter.getActivateInterval(),     // activationInterval 激活间隔
                                    null);
                        }
                    }
                    if (!key.equals("default") & isStartTransport) {
                        continue;
                    }
                    System.err.println("service\t" + transportParameter.getService() + "\tnetwork\t"
                            + transportParameter.getNetwork()
                            + "\tdaemon\t" + transportParameter.getDaemon() + "\t启动成功");
                    logger.debug("service\t" + transportParameter.getService() + "\tnetwork\t"
                            + transportParameter.getNetwork()
                            + "\tdaemon\t" + transportParameter.getDaemon() + "\t启动成功");
                } catch (TibrvException e) {
                    logger.error("Failed to create TibrvRvdTransport:\n" +
                            "service:" + transportParameter.getService() +
                            "\tNetwork:" + transportParameter.getNetwork() +
                            "\tDaemon:" + transportParameter.getDaemon() +
                            "\t" + "is not connect" + e.getLocalizedMessage(), e.getCause());
                    //如果这个备份机不可用，就启动其他的，
                    isStartTransport = false;
                    continue;//启用备用机组
                }
                isStartTransport = transport.isValid();//确认rv机组是否打开
                isFirstFlag = false;


                // Create a response queue
                try {
                    tibrvQueue = transportParameter.isStartInbox() ? new TibrvQueue() : Tibrv.defaultQueue();
                } catch (TibrvException e) {
                    logger.error("Failed to create TibrvQueue:" + e.getLocalizedMessage(), e.getCause());
                    System.exit(0);
                }
                String query_subjectName = transportParameter.getQuerySubjectName();
                String inbox_subjectName = null;
                if (transportParameter.isStartInbox()) {
                    //如果开启点对点通信，就创建
                    // Create an inbox subject for communication with the server and
                    // create a listener for this response subject.
                    //创建与服务器通信的收件箱主题，并为此响应主题创建侦听器。
                    try {
                        inbox_subjectName = transportParameter.getQueryInbox();//创建inbox监听
                        new TibrvListener(tibrvQueue, messageRead, transport, inbox_subjectName, null);
                        logger.debug("start inbox TibrvListener\t" + inbox_subjectName);
                    } catch (TibrvException e) {
                        logger.error("Failed to create listener:\t" + inbox_subjectName
                                + e.getLocalizedMessage(), e.getCause());
                        System.exit(0);
                    }

                    // Create a message for the query.服务器上注册
                    TibrvMsg query_msg = new TibrvMsg();
                    try {
                        query_msg.setSendSubject(query_subjectName);
                        query_msg.setReplySubject(inbox_subjectName);
                        transport.send(query_msg);
                        logger.debug("start TibrvListener\t" + query_subjectName);
                    } catch (TibrvException e) {
                        logger.error("Failed to set send subject:\t" + query_subjectName
                                + e.getLocalizedMessage(), e.getCause());
                        System.exit(0);
                    }

                } else {
                    // Create listeners for specified subjects
                    for (String subjectName : transportParameter.getSubject()) {
                        // create listener using default queue
                        try {
                            TibrvListener tibrvListener = new TibrvListener(tibrvQueue, messageRead, transport,
                                    subjectName, null);
                            transportParameter.setTibrvListenerMap((TibrvRvdTransport) transport, tibrvListener);
                            logger.info("Listening on: " + subjectName);
                        } catch (TibrvException e) {
                            logger.error("Failed to create listener:\t" + subjectName
                                    + e.getLocalizedMessage(), e.getCause());
                            System.exit(0);

                        }
                    }
                }
                this.transport = transport;
            }
        }

        // dispatch Tibrv events
        while (true) {
            try {
                tibrvQueue.dispatch();
            } catch (TibrvException e) {
                logger.error("Exception dispatching default queue:\t" + e.getLocalizedMessage(), e.getCause());
                System.exit(0);
            } catch (InterruptedException ie) {
                logger.error(ie.getLocalizedMessage(), ie.getCause());
                System.exit(0);
            }
        }

    }


    public void onMsg(TibrvListener listener, TibrvMsg msg) {
        System.out.println((new Date()).toString() +
                ": subject=" + msg.getSendSubject() +
                ", reply=" + msg.getReplySubject() +
                ", message=" + msg.toString()
        );
        //清空缓冲区，并将信息立即送出
        System.out.flush();
    }

    public void onFtAction(TibrvFtMember member, String ftgroupName, int action) {
        if (action == TibrvFtMember.PREPARE_TO_ACTIVATE) {
            System.out.println("TibrvFtMember.PREPARE_TO_ACTIVATE invoked...");
        } else if (action == TibrvFtMember.ACTIVATE) {
            System.out.println("TibrvFtMember.ACTIVATE invoked...");
        } else if (action == TibrvFtMember.DEACTIVATE) {
            System.out.println("TibrvFtMember.DEACTIVATE invoked...");
        }
    }

    public void onTimer(TibrvTimer tibrvTimer) {
        System.out.println("定时器");
    }

    public void onFtMonitor(TibrvFtMonitor ftMonitor, String ftgroupName, int numActive) {
        System.out.println("RV挂掉");
    }

    /**
     * 创建实例化的tTibrvRvdTransport数组
     *
     * @param service
     * @param network
     * @param daemon
     * @param subject
     */
    public TibrvRvdTransportParameter setTransportParameter(String groupName, String messageName, String service, String network,
                                                            String daemon, boolean isStartInbox, String... subject) {
        TibrvRvdTransportParameter parameter = new TibrvRvdTransportParameter(messageName, service, network, daemon, subject);
        Map<String, List<TibrvRvdTransportParameter>> listMap = getTransportGroup();

        if (groupName == null) {
            groupName = parameter.getGroupName();
        }
        List<TibrvRvdTransportParameter> parameterList = listMap.get(groupName);
        parameter.setStartInbox(isStartInbox);
        parameter.setGroupName(groupName);
        parameter.setMessageName(messageName);
        parameter.setSubject(subject);
        if (parameterList == null) {
            parameterList = new ArrayList<>();
            listMap.put(groupName, parameterList);
        }

        parameterList.add(parameter);
        logger.debug("groupName\t" + groupName + "\tmessageName\t" + messageName
                + "\tservice\t" + service
                + "\tnetwork\t" + network
                + "\tdaemon\t" + daemon
                + "\tisStartInbox\t" + isStartInbox
                + "\tservice\t" + service
                + "\tsubjects\t" + Arrays.toString(subject)
        );
        return parameter;
    }

    /**
     * 添加
     *
     * @param groupName
     * @param parameter
     * @param messageName
     * @param isStartInbox
     * @param subject
     * @return
     */
    public TibrvRvdTransportParameter setTransport(String groupName, TibrvRvdTransportParameter parameter,
                                                   String messageName, boolean isStartInbox, String... subject) {
        Map<String, List<TibrvRvdTransportParameter>> listMap = getTransportGroup();

        if (groupName == null) {
            groupName = parameter.getGroupName();
        }
        List<TibrvRvdTransportParameter> parameterList = listMap.get(groupName);
        parameter.setStartInbox(isStartInbox);
        parameter.setGroupName(groupName);
        parameter.setMessageName(messageName);
        parameter.setSubject(subject);
        if (parameterList == null) {
            parameterList = new ArrayList<>();
            listMap.put(groupName, parameterList);
        }

        parameterList.add(parameter);
        logger.debug("groupName\t" + groupName + "\tmessageName\t" + messageName
                + "\tservice\t" + parameter.getService()
                + "\tnetwork\t" + parameter.getNetwork()
                + "\tdaemon\t" + parameter.getDaemon()
                + "\tisStartInbox\t" + isStartInbox
                + "\tsubjects\t" + Arrays.toString(subject)
        );
        return parameter;
    }

    /**
     * 带有主备机制的消息组
     *
     * @param transportGroup
     * @param messageName
     * @param subject
     */
    public void setTransportParameterGroup(Map<String, List<String[]>> transportGroup, String messageName, boolean isStartInbox, String... subject) {
        for (String key : transportGroup.keySet()) {
            List<String[]> transportParameterList = transportGroup.get(key);
            for (String[] parameterArr : transportParameterList) {
                setTransportParameter(key, messageName, parameterArr[0], parameterArr[1], parameterArr[2], isStartInbox, subject);
            }
        }
    }

    /**
     * 带有主备机制的消息组(对象)
     *
     * @param transportGroup
     * @param messageName
     * @param isStartInbox
     * @param subject
     */
    public void setTransportObjGroup(Map<String, List<TibrvRvdTransportParameter>> transportGroup,
                                     String messageName, boolean isStartInbox, String... subject) {
        for (String key : transportGroup.keySet()) {
            for (TibrvRvdTransportParameter parameterArr : transportGroup.get(key)) {
                setTransport(key, parameterArr, messageName, isStartInbox, subject);
            }
        }
    }

    public MessageReadCallback getMessageRead() {
        return messageRead;
    }

    public void setMessageRead(MessageReadCallback messageRead) {
        this.messageRead = messageRead;
    }


    public void setTransportGroup(Map<String, List<TibrvRvdTransportParameter>> transportGroup) {
        this.transportGroup = transportGroup;
    }

    public Map<String, List<TibrvRvdTransportParameter>> getTransportGroup() {
        if (transportGroup == null) {
            transportGroup = new HashMap<>();
        }
        return transportGroup;
    }

    public Map<TibrvRvdTransportParameter, Long> getTimerIntervalMessageMap() {
        if (timerIntervalMessageMap == null) {
            timerIntervalMessageMap = new HashMap<>();
        }
        return timerIntervalMessageMap;
    }

    public void setTimerIntervalMessageMap(TibrvRvdTransportParameter timer, long messageTime) {
        getTimerIntervalMessageMap().put(timer, messageTime);
    }

    public TibrvTransport getTransport() {
        return transport;
    }

    public void setTransport(TibrvTransport transport) {
        this.transport = transport;
    }
}
