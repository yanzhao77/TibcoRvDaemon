package com.chot;

import com.chot.messageCheck.XmlReadForCheck;

public class RVDemoMain {

    public static void main(String[] args) throws ClassNotFoundException {
        // prod监听
        String service = "8400";
        String network = ";225.16.16.4";
        String daemon = "tcp:10.50.10.66:7500";

        // 监听多个server
        String subjectACFCNMsvr = "CHOT.G86.ACFMES.PROD.CNMsvr";
        String subjectOCCNMsvr = "CHOT.G86.OCMES.PROD.CNMsvr";
        String subjectCommonCNMsvr = "CHOT.G86.MES.PROD.CNMsvr";

        String messageName = "GetOicMainLotList";// 要拦截的message的名称

        XmlReadForCheck xmlReadStr = new XmlReadForCheck();
        xmlReadStr.init(messageName, service, network, daemon,
                subjectACFCNMsvr, subjectOCCNMsvr, subjectCommonCNMsvr);
    }
    
    //跨网段监听
    //rv主备切换监听，程序报错
   //inbox监听
    //验证是否daeon是否活着
    //控制rv线程是否超出
}
