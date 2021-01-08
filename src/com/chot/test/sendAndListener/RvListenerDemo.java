package com.chot.test.sendAndListener;

import com.chot.entity.daesonEntity.TibrvRvdTransportParameter;
import com.chot.messageCheck.MessageReadCallback;
import com.chot.utils.LoggerUtil;
import com.tibco.tibrv.*;
import org.apache.log4j.Logger;

import java.util.*;

public class RvListenerDemo {
    MessageReadCallback messageRead;//统一的消息处理
    Logger logger;

    public RvListenerDemo() {
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
    public void start(TibrvRvdTransportParameter transportParameter) {
        TibrvQueue tibrvQueue = Tibrv.defaultQueue();
        if (messageRead == null) {
            messageRead = new MessageReadCallback() {
                @Override
                public void onMsg(TibrvListener tibrvListener, TibrvMsg tibrvMsg) {
                    RvListenerDemo.this.onMsg(tibrvListener, tibrvMsg);
                }

                @Override
                public void onError(Object o, int i, String s, Throwable throwable) {
                    {
                        RvListenerDemo.this.onError(o, i, s, throwable);
                    }
                }
            };
        }
        TibrvTransport transport = null;
        Tibrv.setErrorCallback(messageRead);

        try {
            transport = transportParameter.getTibrvRvdTransport();
        } catch (TibrvException e) {
            logger.error(e.getLocalizedMessage());
        }

        // Create listeners for specified subjects
        for (String subjectName : transportParameter.getSubject()) {
            // create listener using default queue
            setTibrvListener(transport, tibrvQueue, subjectName, transportParameter);
        }
        setWarnAndErrorSubject(transport, tibrvQueue, transportParameter);

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
//        String warnSubject = "_RV.WARN.SYSTEM.CLIENT.DEFUNCT";
//        String errorSubject = "_RV.ERROR.SYSTEM.CLIENT.DEFUNCT";
        setTibrvListener(transport, tibrvQueue, "_RV.>", transportParameter);
    }


    public void onMsg(TibrvListener listener, TibrvMsg msg) {
        System.out.println((new Date()).toString() +
                ": subject=" + msg.getSendSubject() +
                ", reply=" + msg.getReplySubject() +
                ", message=" + msg.toString()
        );
        logger.error(msg.getSendSubject() + "\t"
                + msg.toString());
        //清空缓冲区，并将信息立即送出
        System.out.flush();
    }

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
        if (groupName == null) {
            groupName = parameter.getGroupName();
        }
        parameter.setStartInbox(isStartInbox);
        parameter.setGroupName(groupName);
        parameter.setMessageName(messageName);
        parameter.setSubject(subject);

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
    public void setTransportParameterGroup(Map<String, List<String[]>> transportGroup, String messageName,
                                           boolean isStartInbox, String... subject) {
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

    public static void main(String[] args) {
        RvListenerDemo rvListenerDemo = new RvListenerDemo();

        String service = "1000";
        String network =";225.1.1.1";
        String daemons = "10.56.200.238:7500";

//        String service = "7500";
//        String network = ";225.1.1.1";
//        String daemons = "10.56.14.176:7500";

        String subject2 = "DEMO.Demosvr";
        TibrvRvdTransportParameter transportParameter = rvListenerDemo.setTransportParameter(null,
                null, service, network, daemons, false, subject2);
        rvListenerDemo.start(transportParameter);
    }
}
