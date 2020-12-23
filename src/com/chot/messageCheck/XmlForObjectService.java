package com.chot.messageCheck;

import com.chot.rvLister.Rvlistener;
import com.chot.utils.CustomThreadPoolExecutor;
import com.chot.utils.XStreamUtil;

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


    public XmlForObjectService() {

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
    public void rvListenerInit(String checkMessageName, String service, String network, String daemon, String... subjectNames) {
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                XmlReadFactory xmlReadForCheck = new XmlReadFactory();
                xmlReadForCheck.init(checkMessageName, service, network, daemon, subjectNames);
            }
        });
        thread.start();
    }
}
