package com.chot;


import com.chot.messageCheck.XmlForObjectService;

import java.util.ArrayList;


public class RVDemoMain {

    public static void main(String[] args) throws ClassNotFoundException {
        XmlForObjectService xmlReadStr = new XmlForObjectService();

        // prod监听
        String service = "8400";
        String network = ";225.16.16.4";
        String daemon = "tcp:10.50.10.66:7500";


        // 监听多个subject
        String subjectACFCNMsvr = "CHOT.G86.ACFMES.PROD.CNMsvr";
        String subjectOCCNMsvr = "CHOT.G86.OCMES.PROD.CNMsvr";
        String subjectCommonCNMsvr = "CHOT.G86.MES.PROD.CNMsvr";
        String messageName = "GetOicMainLotList";// 要拦截的message的名称
        xmlReadStr.rvListenerInit(messageName, service, network, daemon, false,
                subjectACFCNMsvr, subjectOCCNMsvr, subjectCommonCNMsvr);

        //再启动一个监听
        String serviceTEST = "8200";
        String networkTEST = ";225.16.16.2";
        String daemonTEST = "10.50.10.72:7500";
        String subjectACFTESTsvr = "CHOT.G86.ACFMES.PROD.PEMsvr";
        String subjectOCTESTsvr = "CHOT.G86.OCMES.PROD.PEMsvr";
        String subjectCommonTESTsvr = "CHOT.G86.MES.PROD.PEMsvr";

        String messageNameTEST = "CheckRecipeParameterRequest";
        xmlReadStr.rvListenerInit(messageNameTEST, serviceTEST, networkTEST, daemonTEST, false,
                subjectACFTESTsvr, subjectOCTESTsvr, subjectCommonTESTsvr);

        //启动主备机制，如果启动时报错，就切换到备用机
//        ArrayList<String[]> serverList = new ArrayList<>();
//        serverList.add(new String[]{service, network, daemon});
//        serverList.add(new String[]{serviceTEST, networkTEST, daemonTEST});
//        xmlReadStr.rvListenerGroupInit("PEMsvr", messageNameTEST, serverList, false,
//                subjectACFTESTsvr, subjectCommonTESTsvr, subjectOCTESTsvr);

        String service2 = "7500";
        String network2 = ";225.1.1.1";
        String daemon2 = "tcp:7500";
        String subject2 = "DEMO.FT.NUM";
        xmlReadStr.rvListenerInit(null, service2, network2, daemon2, false,
                subject2);
        xmlReadStr.start();
    }

}
