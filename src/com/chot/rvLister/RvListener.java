package com.chot.rvLister;

import com.chot.messageCheck.MessageReadCallback;
import com.chot.entity.daesonEntity.TibrvRvdTransportParameter;
import com.chot.utils.LoggerUtil;
import com.tibco.tibrv.*;
import org.apache.log4j.Logger;

import java.util.*;

public class RvListener {
    MessageReadCallback messageRead;//统一的消息处理
    Logger logger;
    //如果这里是启动多个service，就是默认的groupKey，如果是主备机制，就是设置的groupName
    Map<String, List<TibrvRvdTransportParameter>> transportGroup;//多个server
    TibrvTransport transport;//当前正在运行的transport,(主备切换)

    public RvListener() {
        logger = LoggerUtil.getLogger();
        // open Tibrv in native implementation
        try {
            Tibrv.open(Tibrv.IMPL_NATIVE);//打开本机上的rv进程
        } catch (TibrvException e) {
            logger.error("Failed to open Tibrv in native implementation:" + e.getLocalizedMessage(), e.getCause());
            System.exit(0);
        }
    }


    /**
     * 启动监听
     */
    public void start() {
        TibrvQueue tibrvQueue = Tibrv.defaultQueue();//获取一个默认的队列
        if (messageRead == null) {
            messageRead = new MessageReadCallback() {
                @Override
                public void onMsg(TibrvListener tibrvListener, TibrvMsg tibrvMsg) {
                    RvListener.this.onMsg(tibrvListener, tibrvMsg);
                }

                @Override
                public void onError(Object o, int i, String s, Throwable throwable) {
                    {
                        RvListener.this.onError(o, i, s, throwable);
                    }
                }
            };
        }
        boolean isStartTransport = false;//判断是否启动成功
        for (String key : transportGroup.keySet()) {
            isStartTransport = false;
            List<TibrvRvdTransportParameter> transportParameterList = new ArrayList<>();
            transportParameterList.addAll(transportGroup.get(key));
            //如果key是默认机组，那么启动所有的Transport，如果不是，就只启动一个
            for (TibrvRvdTransportParameter transportParameter : transportParameterList) {
                // Create RVD transport
                TibrvTransport transport = null;
                try {
                    Tibrv.setErrorCallback(messageRead);
                    transport = transportParameter.getTibrvRvdTransport();
                    if (!key.equals("default") & isStartTransport) {
                        continue;//如果是多个service启动，则全部启动；如果主机已经启动成功，就跳出，如果没启动，就继续启动备用机
                    }
                    transportParameter.setValidityFlag(true);//设置该service可用
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
                    transportParameter.setValidityFlag(false);
                    isStartTransport = false;
                    continue;//启用备用机组
                }
                isStartTransport = transport.isValid();//确认rv机组是否打开

                // Create a response queue
                try {
                    tibrvQueue = transportParameter.isStartInbox() ? new TibrvQueue() : Tibrv.defaultQueue();
                } catch (TibrvException e) {
                    logger.error("Failed to create TibrvQueue:" + e.getLocalizedMessage(), e.getCause());
                    System.exit(0);
                }

                //获取主监听参数，如果这里是单对单监听，就只取出一个
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
                    transportParameter.setStartListener(true);

                    // Create a message for the query.服务器上注册
                    TibrvMsg query_msg = new TibrvMsg();
                    try {
                        query_msg.setSendSubject(query_subjectName);
                        query_msg.setReplySubject(inbox_subjectName);
                        transport.send(query_msg);//向服务器确认子机链接
                        logger.debug("start TibrvListener\t" + query_subjectName);
                    } catch (TibrvException e) {
                        logger.error("Failed to set send subject:\t" + query_subjectName
                                + e.getLocalizedMessage(), e.getCause());
                        System.exit(0);
                    }

                } else {
                    //启动所有的监听频道
                    // Create listeners for specified subjects
                    for (String subjectName : transportParameter.getSubject()) {
                        // create listener using default queue
                        setTibrvListener(transport, tibrvQueue, subjectName, transportParameter);
                    }
                    transportParameter.setStartListener(true);//确认打开监听
                }
                this.transport = transport;
                //启动RV系统异常频道监听
                setWarnAndErrorSubject(transport, tibrvQueue, transportParameter);
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

    /**
     * 创建监听
     *
     * @param transport
     * @param tibrvQueue
     * @param subjectName
     * @param transportParameter
     */
    public void setTibrvListener(TibrvTransport transport, TibrvQueue tibrvQueue, String subjectName,
                                 TibrvRvdTransportParameter transportParameter) {
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

    /**
     * 添加错误频道监听
     *
     * @param transport
     * @param tibrvQueue
     * @param transportParameter
     */
    public void setWarnAndErrorSubject(TibrvTransport transport, TibrvQueue tibrvQueue, TibrvRvdTransportParameter
            transportParameter) {
//        String[] subjectArr = new String[]{"_RV.ERROR.>", "_RV.WARN.>"};
//        for (String subject : subjectArr) {
//            setTibrvListener(transport, tibrvQueue, subject, transportParameter);
//        }
        setTibrvListener(transport, tibrvQueue, "_RV.>", transportParameter);
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
     * 异常监听
     *
     * @param o
     * @param i
     * @param s
     * @param throwable
     */
    private void onError(Object o, int i, String s, Throwable throwable) {
        System.out.println(o + "\t" + i + "\t" + s);
        logger.error(throwable.getLocalizedMessage());
    }

    /**
     * 创建实例化的tTibrvRvdTransport数组
     *
     * @param service
     * @param network
     * @param daemon
     * @param subject
     */
    public TibrvRvdTransportParameter setTransportParameter(String groupName, String messageName, String
            service, String network, String daemon, boolean isStartInbox, String... subject) {
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
    public void setTransportParameterGroup(Map<String, List<String[]>> transportGroup, String messageName,
                                           boolean isStartInbox, String... subject) {
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

    public TibrvTransport getTransport() {
        return transport;
    }

    public void setTransport(TibrvTransport transport) {
        this.transport = transport;
    }
}
