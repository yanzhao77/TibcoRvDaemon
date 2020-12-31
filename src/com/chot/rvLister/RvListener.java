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
    /**
     * 这里是两层结构，Map的key是它的group名字，如果有备份的机组，就使用单个
     */
    Map<String, List<TibrvRvdTransportParameter>> transportGroup;//多个server

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
                    onMsgCallBack(tibrvListener, tibrvMsg);
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
                if (!key.equals("default") & isStartTransport) {
                    continue;
                }
                // Create RVD transport
                TibrvTransport transport = null;
                try {
                    transport = transportParameter.getTibrvRvdTransport();
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
                } else {
                    // Create listeners for specified subjects
                    for (String subjectName : transportParameter.getSubject()) {
                        // create listener using default queue
                        try {
                            new TibrvListener(tibrvQueue, messageRead, transport,
                                    subjectName, null);
                            logger.info("Listening on: " + subjectName);
                        } catch (TibrvException e) {
                            logger.error("Failed to create listener:\t" + subjectName
                                    + e.getLocalizedMessage(), e.getCause());
                            System.exit(0);

                        }
                    }
                }
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
     * 消息接收处理放在XmlReadFactory
     *
     * @param listener
     * @param msg
     */
    @Deprecated
    public void onMsgCallBack(TibrvListener listener, TibrvMsg msg) {
        System.out.println((new Date()).toString() +
                ": subject=" + msg.getSendSubject() +
                ", reply=" + msg.getReplySubject() +
                ", message=" + msg.toString()
        );
        //清空缓冲区，并将信息立即送出
        System.out.flush();
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

        List<TibrvRvdTransportParameter> parameterList;
        if (groupName == null) {
            groupName = parameter.getGroupName();
        }
        parameter.setStartInbox(isStartInbox);
        parameter.setGroupName(groupName);
        parameterList = listMap.get(groupName);

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
}
