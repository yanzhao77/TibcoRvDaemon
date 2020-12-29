package com.chot.rvLister;

import com.chot.messageCheck.MessageReadCallback;
import com.chot.entity.daesonEntity.TibrvRvdTransportParameter;
import com.tibco.tibrv.*;

import java.util.*;

public class RvListener implements TibrvMsgCallback {

    MessageReadCallback messageRead;//统一的消息处理
    /**
     * 这里是两层结构，Map的key是它的group名字，如果有备份的机组，就使用单个
     */
    Map<String, List<TibrvRvdTransportParameter>> transportGroup;//多个server

    public RvListener() {
        // open Tibrv in native implementation
        try {
            Tibrv.open(Tibrv.IMPL_NATIVE);
        } catch (TibrvException e) {
            System.err.println("Failed to open Tibrv in native implementation:");
            e.printStackTrace();
            System.exit(0);
        }
    }


    /**
     * 启动监听
     */
    public void start() {
        TibrvQueue tibrvQueue = Tibrv.defaultQueue();

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
                    isStartTransport = true;
                } catch (TibrvException e) {
                    System.err.println("Failed to create TibrvRvdTransport:\n" +
                            "service:" + transportParameter.getService() +
                            "\tNetwork:" + transportParameter.getNetwork() +
                            "\tDaemon:" + transportParameter.getDaemon() +
                            "\t" + "is not connect");
                    //如果这个备份机不可用，就启动其他的，
                    isStartTransport = false;
                    continue;//启用备用机组
                }


                // Create a response queue
                try {
                    tibrvQueue = transportParameter.isStartInbox() ? new TibrvQueue() : Tibrv.defaultQueue();
                } catch (TibrvException e) {
                    System.err.println("Failed to create TibrvQueue:");
                    e.printStackTrace();
                    System.exit(0);
                }
                String query_subjectName = transportParameter.getQuerySubjectName();
                String response_subjectName = null;
                if (transportParameter.isStartInbox()) {
                    //如果开启点对点通信，就创建
                    // Create an inbox subject for communication with the server and
                    // create a listener for this response subject.
                    //创建与服务器通信的收件箱主题，并为此响应主题创建侦听器。
                    try {
                        response_subjectName = transportParameter.getQueryInbox();
                        new TibrvListener(tibrvQueue,//创建inbox监听
                                this, transport, response_subjectName, null);
                    } catch (TibrvException e) {
                        System.err.println("Failed to create listener:");
                        e.printStackTrace();
                        System.exit(0);
                    }

                    // Create a message for the query.
                    TibrvMsg query_msg = new TibrvMsg();
                    try {
                        query_msg.setSendSubject(query_subjectName);
                    } catch (TibrvException e) {
                        System.err.println("Failed to set send subject:");
                        e.printStackTrace();
                        System.exit(0);
                    }

                    TibrvMsg reply_msg = null;
                    //向主机发送消息，并接受消息，确认连通
                    try {
                        reply_msg = transport.sendRequest(query_msg, 10);
                    } catch (TibrvException e) {
                        System.err.println("Failed to detect server:");
                        e.printStackTrace();
                        System.exit(0);
                    }

                    // If timeout, reply message is null and query failed.
                    if (reply_msg == null) {
                        System.err.println("Failed to detect server.");
                        System.exit(0);
                    }
                    // Report finding a server.
                    TibrvMsg server_msg = new TibrvMsg();
                    String server_subject = reply_msg.getReplySubject();
                    System.out.println("tibrvclient successfully located a server: " +
                            server_subject);
                    // Create a dispatcher with 5 second timeout to process server replies
                    TibrvDispatcher dispatcher = new TibrvDispatcher("Dispatcher", tibrvQueue, 5.0);

                    try {
                        server_msg.setSendSubject(server_subject);
                        server_msg.setReplySubject(response_subjectName);
                    } catch (TibrvException e) {
                        System.err.println("Failed to set subjects, fields for test message:");
                        e.printStackTrace();
                        System.exit(0);
                    }
                } else {
                    // Create listeners for specified subjects
                    for (String subjectName : transportParameter.getSubject()) {
                        // create listener using default queue
                        try {
                            System.out.println("是否启动？：\t" + transport.isValid());
                            new TibrvListener(tibrvQueue, this, transport,
                                    subjectName, null);
                            System.err.println("Listening on: " + subjectName);
                        } catch (TibrvException e) {
                            System.err.println("Failed to create listener:");
                            e.printStackTrace();
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
                System.err.println("Exception dispatching default queue:");
                e.printStackTrace();
                System.exit(0);
            } catch (InterruptedException ie) {
                System.exit(0);
            }
        }

    }

    public void onMsg(TibrvListener listener, TibrvMsg msg) {
//        System.out.println((new Date()).toString() +
//                ": subject=" + msg.getSendSubject() +
//                ", reply=" + msg.getReplySubject() +
//                ", message=" + msg.toString()
//        );
        if (messageRead != null)
            messageRead.onMsg(listener, msg);
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
