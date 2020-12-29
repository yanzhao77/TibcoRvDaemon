package com.chot.service.serviceImpl;

import com.chot.service.XmlForObjectService;

import java.util.List;


/**
 * 启动接口
 *
 * @version 1.0
 * @Classname XmlForObjectService
 * @Description TODO
 * @Date 2020/12/9 19:17
 * @Created by yan34177
 */
public class XmlForObjectServiceImpl implements XmlForObjectService {
    private XmlReadFactory xmlReadForCheck;

    public XmlForObjectServiceImpl() {
        xmlReadForCheck = new XmlReadFactory();
    }


    /**
     * 加载监听对象参数
     *
     * @param checkMessageName 监听的消息名称
     * @param service
     * @param network
     * @param daemon
     * @param isStartInbox     是否开启inbox
     * @param subjectNames     监听的频道参数
     */
    public void rvListenerInit(String checkMessageName, String service, String network,
                               String daemon, boolean isStartInbox, String... subjectNames) {
        xmlReadForCheck.init(checkMessageName, service, network, daemon, isStartInbox, subjectNames);
    }

    /**
     * 主备机组监听
     *
     * @param groupName
     * @param checkMessageName
     * @param serviceList
     * @param isStartInbox
     * @param subjectNames
     */
    public void rvListenerGroupInit(String groupName, String checkMessageName, List<String[]> serviceList,
                                    boolean isStartInbox, String... subjectNames) {
        xmlReadForCheck.initGroups(groupName, checkMessageName, serviceList, isStartInbox, subjectNames);
    }

    /**
     * 启动监听
     */
    public void start() {
        xmlReadForCheck.start();
    }
}
