package com.chot.service;

import com.chot.entity.daesonEntity.TibrvRvdTransportParameter;
import com.chot.messageCheck.MessageReadCallback;
import com.chot.rvLister.RvListener;
import com.chot.utils.CustomThreadPoolExecutor;
import com.chot.xmlService.XMLService;
import com.tibco.tibrv.*;

import org.apache.log4j.Logger;

import java.util.*;

public class XmlReadFactory {
    MessageReadCallback messageRead;// 设置回调验证消息
    CustomThreadPoolExecutor customThreadPoolExecutor;//线程池
    XMLService xmlService;
    RvListener rvlistener;
    Logger logger;


    public XmlReadFactory(Logger logger) {
        this.logger = logger;
        rvlistener = new RvListener();
        xmlService = new XMLService();
        customThreadPoolExecutor = new CustomThreadPoolExecutor();
        customThreadPoolExecutor.init();
        messageRead = new MessageReadCallback() {
            @Override
            public void onMsg(TibrvListener tibrvListener, TibrvMsg tibrvMsg) {
                //如果有消息传入，则分配一个线程执行消息处理
                customThreadPoolExecutor.execute(new Runnable() {
                    @Override
                    public void run() {
                        System.out.println(tibrvListener.getTransport() + "\t" + tibrvMsg.toString());
//                        xmlService.xmlCheckForMessage(tibrvMsg.toString(), tibrvListener, tibrvMsg);

                        // 如果xStream无法识别，就使用map解析
                        // Map<String, Object> objectMap =
                        // ParseXML.parserXml(message);
                        // messageValue = objectMap;
                    }
                });
            }

            @Override
            public void onFtAction(TibrvFtMember member, String groupName, int action) {
                System.err.println("主备切换..............");
                if (action == TibrvFtMember.PREPARE_TO_ACTIVATE) {
                    //准备激活
                    System.err.println("TibrvFtMember.PREPARE_TO_ACTIVATE invoked...准备激活");
                    System.out.println("*** PREPARE TO ACTIVATE: " + groupName);
                } else if (action == TibrvFtMember.ACTIVATE) {
                    //立即激活
                    System.err.println("TibrvFtMember.ACTIVATE invoked...立即激活");
                    System.out.println("*** ACTIVATE: " + groupName);
                    try {
                        enableListener(member);
                    } catch (TibrvException e) {
                        logger.error(e.getLocalizedMessage());
                    }
                } else if (action == TibrvFtMember.DEACTIVATE) {
                    //立即停用
                    System.err.println("TibrvFtMember.DEACTIVATE invoked...立即停用");
//                    System.out.println("*** DEACTIVATE: " + groupName);
                    try {
                        disableListener(member);
                    } catch (TibrvException e) {
                        logger.error(e.getLocalizedMessage());
                    }
                }
            }
        };
    }

    void enableListener(TibrvFtMember member) throws TibrvException {
        // Subscribe to subject

//            listener = new TibrvListener(Tibrv.defaultQueue(), this, transport, subject, null);
//            System.out.println("Start Listening on: " + subject);
        List<TibrvRvdTransportParameter> rvdTransportParameterList = rvlistener.getTransportGroup().get(member.getGroupName());
        if (rvdTransportParameterList != null) {
            for (TibrvRvdTransportParameter tibrvRvdTransportParameter : rvdTransportParameterList) {
                if (tibrvRvdTransportParameter.getTibrvRvdTransport() == member.getTransport()) {
                    for (String subject : tibrvRvdTransportParameter.getSubject()) {
                        TibrvListener tibrvListener = new TibrvListener(Tibrv.defaultQueue(), rvlistener.getMessageRead(), member.getTransport(), subject, null);
                        tibrvRvdTransportParameter.setTibrvListenerMap((TibrvRvdTransport) member.getTransport(), tibrvListener);
                        System.out.println("Start Listening on: " + subject);
                    }
                }
            }
        }
        System.out.println("Start Listening on: " + member.getGroupName());
    }

    void disableListener(TibrvFtMember member) throws TibrvException {
//        listener.destroy();
//        System.out.println("Destroy Listener on Subject: " + subject);
        List<TibrvRvdTransportParameter> rvdTransportParameterList = rvlistener.getTransportGroup().get(member.getGroupName());
        if (rvdTransportParameterList != null) {
            for (TibrvRvdTransportParameter tibrvRvdTransportParameter : rvdTransportParameterList) {
                if (tibrvRvdTransportParameter.getTibrvRvdTransport() != member.getTransport()) {
                    for (TibrvRvdTransport tibrvRvdTransport : tibrvRvdTransportParameter.getTibrvListenerMap().keySet()) {
                        if (tibrvRvdTransport != member.getTransport()) {
                            tibrvRvdTransportParameter.getTibrvListenerMap().get(tibrvRvdTransport).destroy();
                        }
                    }

                }
            }
        }
        System.out.println("Destroy Listener on Subject: " + member.getGroupName());
    }

    /**
     * 启动监听
     */
    public void start() {
        rvlistener.start();
        logger.debug("start rvlistener");
    }

    /**
     * 初始化监听，并启动监听
     *
     * @param checkMessageName 监听的消息名称
     * @param service
     * @param network
     * @param daemon
     * @param isStartInbox
     * @param subjectNames     监听的频道参数
     */
    public void init(String checkMessageName, String service, String network, String daemon, boolean isStartInbox, String... subjectNames) {
        xmlService.rvlistenerInit(checkMessageName, subjectNames);
        rvlistener.setMessageRead(getMessageRead());
        rvlistener.setTransportParameter(null, checkMessageName, service, network, daemon, isStartInbox, subjectNames);


    }

    /**
     * 初始化机组监听，并启动监听
     *
     * @param groupName        主备机组名称
     * @param checkMessageName 监听的消息名称
     * @param serviceArr
     * @param isStartInbox
     * @param subjectNames     监听的频道参数
     */
    public void initGroups(String groupName, String checkMessageName, String[][] serviceArr, boolean isStartInbox, String... subjectNames) {
        xmlService.rvlistenerInit(checkMessageName, subjectNames);
        rvlistener.setMessageRead(getMessageRead());
        Map<String, List<String[]>> stringListMap = new HashMap<>();
        List<String[]> serviceList = new ArrayList<>();
        for (String[] strings : serviceArr) {
            serviceList.add(strings);
        }
        stringListMap.put(groupName, serviceList);
        rvlistener.setTransportParameterGroup(stringListMap, checkMessageName, isStartInbox, subjectNames);
    }

    /**
     * 初始化机组监听，并启动监听
     *
     * @param groupName        主备机组名称
     * @param checkMessageName 监听的消息名称
     * @param parameterList
     * @param isStartInbox
     * @param subjectNames     监听的频道参数
     */
    public void initObjGroup(String groupName, String checkMessageName, List<TibrvRvdTransportParameter> parameterList, boolean isStartInbox, String... subjectNames) {
        xmlService.rvlistenerInit(checkMessageName, subjectNames);
        rvlistener.setMessageRead(getMessageRead());
        Map<String, List<TibrvRvdTransportParameter>> parameterListMap = new HashMap<>();
        parameterListMap.put(groupName, parameterList);
        rvlistener.setTransportObjGroup(parameterListMap, checkMessageName, isStartInbox, subjectNames);
    }

    public MessageReadCallback getMessageRead() {
        return messageRead;
    }

    public void setMessageRead(MessageReadCallback messageRead) {
        this.messageRead = messageRead;
    }


}
