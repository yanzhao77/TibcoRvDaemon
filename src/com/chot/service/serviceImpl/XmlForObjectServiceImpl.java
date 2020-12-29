package com.chot.service.serviceImpl;

import com.chot.service.XmlForObjectService;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

import java.io.File;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
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
        xmlReadForCheck = new XmlReadFactory(logger);
        loggerInit();
    }

    public void loggerInit() {
//        Path configrationPath = Paths.get("cfg", "log4j.properties");
//        if (!Files.exists(configrationPath) || !Files.isRegularFile(configrationPath)) {
//            return;
//
//        }
//        System.setProperty("log4j.defaultInitOverride", "1");
//        PropertyConfigurator.configure(configrationPath.toString());
        String resource = System.getProperty("user.dir") + "/resources/" + "log4j.properties";
        PropertyConfigurator.configure(resource);
        logger = Logger.getLogger(XmlForObjectServiceImpl.class.getName());
        logger.info(null);
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
     * 启动监听
     */
    public void start() {
        xmlReadForCheck.start();
    }
}
