package com.chot.service;

import com.chot.entity.daesonEntity.TibrvRvdTransportParameter;

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
     * @param groupName        机组名称
     * @param checkMessageName 监听的消息名称
     * @param serviceArr       主机和备份机参数
     * @param isStartInbox     是否开启inbox
     * @param subjectNames     监听的频道参数
     */
    void rvListenerGroupInit(String groupName, String checkMessageName, String[][] serviceArr,
                             boolean isStartInbox, String... subjectNames);

    /**
     * 主备机组监听
     *
     * @param groupName        机组名称
     * @param checkMessageName 监听的消息名称
     * @param parameterList    主机和备份机参数
     * @param isStartInbox     是否开启inbox
     * @param subjectNames     监听的频道参数
     */
    void rvListenerObjGroupInit(String groupName, String checkMessageName, List<TibrvRvdTransportParameter> parameterList,
                                boolean isStartInbox, String... subjectNames);

    /**
     * 启动监听
     */
    void start();
}
