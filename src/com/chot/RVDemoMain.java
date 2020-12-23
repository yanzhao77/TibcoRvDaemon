package com.chot;


import com.chot.messageCheck.XmlForObjectService;


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
        xmlReadStr.rvListenerInit(messageName, service, network, daemon,
                subjectACFCNMsvr, subjectOCCNMsvr, subjectCommonCNMsvr);

        //再启动一个线程监听
        String serviceTEST = "8200";
        String networkTEST = ";225.16.16.2";
        String daemonTEST = "10.50.10.72:7500";
        String subjectACFTESTsvr = "CHOT.G86.ACFMES.PROD.PEMsvr";
        String subjectOCTESTsvr = "CHOT.G86.OCMES.PROD.PEMsvr";
        String subjectCommonTESTsvr = "CHOT.G86.MES.PROD.PEMsvr";

        String messageNameTEST = "CheckecipeParameterRequest";
        xmlReadStr.rvListenerInit(messageNameTEST, serviceTEST, networkTEST, daemonTEST,
                subjectACFTESTsvr, subjectOCTESTsvr, subjectCommonTESTsvr);
  
    }

}
