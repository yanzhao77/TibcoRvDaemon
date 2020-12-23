package com.chot.messageCheck;

import com.chot.utils.XStreamUtil;
import com.tibco.tibrv.TibrvMsg;

/**
 * @version 1.0
 * @Classname XMLreadService
 * @Description TODO
 * @Date 2020/12/18 11:03
 * @Created by yan34177
 */
public class XMLreadService {
    XStreamUtil xStreamUtil;

    public XMLreadService() {
        xStreamUtil = new XStreamUtil();
    }

    public void toJavaBan(String readMessageCheck, Class cls, TibrvMsg msg) {
        Object messageValue;// 抓取的消息obj
        messageValue = xStreamUtil.toBean(readMessageCheck, cls);
        if (messageValue != null) {
            println(messageValue, msg);// 打印
        }
    }

    /**
     * 打印消息
     *
     * @param messageValue
     * @param message
     */
    public void println(Object messageValue, TibrvMsg message) {
        Thread thread = Thread.currentThread();
        System.err.println("线程ID：" + thread.getId() + "\t线程名称：" + thread.getName());
        System.err.println(message.getSendSubject());
//        if (message instanceof GetOicMainLotList) {
//            GetOicMainLotList messageEntity = (GetOicMainLotList) message;
//
//            System.out.println(messageEntity.getBody().getFactoryName());
//            System.out.println(messageEntity.getBody().getMachineName());
//            System.out.println(messageEntity.getBody().getEventUser());
//            System.out.println(messageEntity.getBody().getSoftwareVersion());
//            System.out.println(messageEntity.getBody()
//                    .getTransactionStartTime());
//            System.out.println();
//        } else if (message instanceof CheckecipeParameterRequest) {
//            CheckecipeParameterRequest checkMessage = new CheckecipeParameterRequest();
//            System.out.println(checkMessage.getHeader().getMessageName());
//            System.out.println(checkMessage.getBody().getLineName());
//        }
    }
}
