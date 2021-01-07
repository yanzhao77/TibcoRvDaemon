package com.chot.service.serviceImpl;

import com.chot.entity.daesonEntity.TibrvRvdTransportParameter;
import com.chot.service.XmlForObjectService;
import com.chot.service.XmlReadFactory;
import com.chot.utils.LoggerUtil;
import org.apache.log4j.Logger;

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
    Logger logger;

    public XmlForObjectServiceImpl() {
        logger = LoggerUtil.getLogger();
        xmlReadForCheck = new XmlReadFactory(logger);

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
     * @param groupName        机组名称
     * @param checkMessageName 监听的消息名称
     * @param serviceArr       主机和备份机参数
     * @param isStartInbox     是否开启inbox
     * @param subjectNames     监听的频道参数
     */
    public void rvListenerGroupInit(String groupName, String checkMessageName, String[][] serviceArr,
                                    boolean isStartInbox, String... subjectNames) {
        xmlReadForCheck.initGroups(groupName, checkMessageName, serviceArr, isStartInbox, subjectNames);
    }

    /**
     * 主备机组监听
     *
     * @param groupName        机组名称
     * @param checkMessageName 监听的消息名称
     * @param parameterList       主机和备份机参数
     * @param isStartInbox     是否开启inbox
     * @param subjectNames     监听的频道参数
     */
    public void rvListenerObjGroupInit(String groupName, String checkMessageName, List<TibrvRvdTransportParameter> parameterList,
                                       boolean isStartInbox, String... subjectNames) {
        xmlReadForCheck.initObjGroup(groupName, checkMessageName, parameterList, isStartInbox, subjectNames);
    }

    /**
     * 启动监听
     */
    public void start() {
        xmlReadForCheck.start();
    }
}
