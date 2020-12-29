package com.chot.service;

import java.util.List;

/**
 * @version 1.0
 * @Classname XmlForObjectService
 * @Description TODO
 * @Date 2020/12/29 17:18
 * @Created by yan34177
 */
public interface XmlForObjectService {

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
    void rvListenerInit(String checkMessageName, String service, String network,
                        String daemon, boolean isStartInbox, String... subjectNames);

    /**
     * 主备机组监听
     *
     * @param groupName
     * @param checkMessageName
     * @param serviceList
     * @param isStartInbox
     * @param subjectNames
     */
    void rvListenerGroupInit(String groupName, String checkMessageName, List<String[]> serviceList,
                             boolean isStartInbox, String... subjectNames);

    /**
     * 启动监听
     */
    void start();
}
