package com.chot.messageCheck;

import com.chot.rvLister.Rvlistener;
import com.chot.utils.CustomThreadPoolExecutor;
import com.chot.utils.XStreamUtil;

import java.util.List;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * 启动接口
 *
 * @version 1.0
 * @Classname XmlForObjectService
 * @Description TODO
 * @Date 2020/12/9 19:17
 * @Created by yan34177
 */
public class XmlForObjectService {
    XmlReadFactory xmlReadForCheck;

    public XmlForObjectService() {
        xmlReadForCheck = new XmlReadFactory();
    }

    /**
     * 启动一条线程，并启动监听
     *
     * @param checkMessageName 监听的消息名称
     * @param service
     * @param network
     * @param daemon
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
