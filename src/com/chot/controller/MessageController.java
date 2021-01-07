package com.chot.controller;

import com.chot.entity.daesonEntity.TibrvRvdTransportParameter;
import com.chot.messageCheck.MessageReadCallback;
import com.chot.rvLister.RvListener;
import com.chot.rvLister.TibrvFtService;
import com.chot.utils.CustomThreadPoolExecutor;
import com.chot.xmlService.XMLService;
import com.tibco.tibrv.*;

import org.apache.log4j.Logger;

import java.util.*;

public class MessageController {
    MessageReadCallback messageRead;// 设置回调验证消息
    CustomThreadPoolExecutor customThreadPoolExecutor;//线程池
    XMLService xmlService;//消息处理
    RvListener rvlistener;//RV监听
    TibrvFtService tibrvFtService;//RV容错机制
    Logger logger;//log输出


    public MessageController(Logger logger) {
        this.logger = logger;
        rvlistener = new RvListener();
        xmlService = new XMLService();
        customThreadPoolExecutor = new CustomThreadPoolExecutor();
        customThreadPoolExecutor.init();
        tibrvFtService = new TibrvFtService(rvlistener, logger);
        messageRead = new MessageReadCallback() {
            @Override
            public void onMsg(TibrvListener tibrvListener, TibrvMsg tibrvMsg) {
                //如果有消息传入，则分配一个线程执行消息处理
                customThreadPoolExecutor.execute(new Runnable() {
                    @Override
                    public void run() {
                        System.out.println(tibrvListener.getTransport() + "\t" + tibrvMsg.toString());
//                        xmlService.xmlCheckForMessage(tibrvMsg.toString(), tibrvListener, tibrvMsg);
                    }
                });
                try {
                    tibrvFtService.setTimerIntervalMessageTime(tibrvListener);
                } catch (TibrvException e) {
                    logger.error(e.getLocalizedMessage());
                }
            }

            @Override
            public void onFtAction(TibrvFtMember member, String groupName, int action) {
                tibrvFtService.onFtAction(member, groupName, action);
            }

            @Override
            public void onTimer(TibrvTimer tibrvTimer) {
                try {
                    tibrvFtService.onTimer(tibrvTimer);
                } catch (TibrvException e) {
                    logger.error(e.getLocalizedMessage());
                }
            }

            @Override
            public void onFtMonitor(TibrvFtMonitor ftMonitor, String ftgroupName, int numActive) {
                tibrvFtService.onFtMonitor(ftMonitor, ftgroupName, numActive);
            }

        };
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
