package com.chot.service.serviceImpl;

import com.chot.utils.XStreamUtil;
import com.tibco.tibrv.TibrvMsg;

/**
 * @version 1.0
 * @Classname XMLreadService
 * @Description TODO
 * @Date 2020/12/18 11:03
 * @Created by yan34177
 */
public class XMLService {
    private XStreamUtil xStreamUtil;

    public XMLService() {
        xStreamUtil = new XStreamUtil();
    }

    public void toJavaBan(String readMessageCheck, Class cls, TibrvMsg msg) {
        Object messageValue = null;// 抓取的消息obj
        try {
            messageValue = xStreamUtil.toBean(readMessageCheck, cls);
        } catch (Exception e) {
            System.err.println("无法识别的message,message有其他字段！");
            System.out.println(readMessageCheck);
            e.printStackTrace();
        }
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
//        if (messageValue instanceof GetOicMainLotList) {
//            GetOicMainLotList messageEntity = (GetOicMainLotList) messageValue;
//
//            System.out.println(messageEntity.getBody().getFactoryName());
//            System.out.println(messageEntity.getBody().getMachineName());
//            System.out.println(messageEntity.getBody().getEventUser());
//            System.out.println(messageEntity.getBody().getSoftwareVersion());
//            System.out.println(messageEntity.getBody()
//                    .getTransactionStartTime());
//            System.out.println();
//        } else if (messageValue instanceof CheckRecipeParameterRequest) {
//            CheckRecipeParameterRequest checkMessage = (CheckRecipeParameterRequest) messageValue;
//            System.out.println(checkMessage.getHeader().getMessageName());
//            System.out.println(checkMessage.getBody().getLineName());
//        }
    }
}
