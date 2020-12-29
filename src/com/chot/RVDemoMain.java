package com.chot;


import com.chot.service.XmlForObjectService;
import com.chot.service.serviceImpl.XmlForObjectServiceImpl;

import java.util.ArrayList;


public class RVDemoMain {

    public static void main(String[] args) throws ClassNotFoundException {
        XmlForObjectService xmlReadStr = new XmlForObjectServiceImpl();

        // prod监听
        String service = "8400";
        String network = ";225.16.16.4";
        String daemon = "tcp:10.50.10.66:7500";
        String ACFCNMsvr = "CHOT.G86.ACFMES.PROD.CNMsvr";
        String OCCNMsvr = "CHOT.G86.OCMES.PROD.CNMsvr";
        String CNMsvr = "CHOT.G86.MES.PROD.CNMsvr";
        String messageName = "GetOicMainLotList";// 要拦截的message的名称
        xmlReadStr.rvListenerInit(messageName, service, network, daemon, false,
                ACFCNMsvr, OCCNMsvr, CNMsvr);


        String serviceTEST = "8200";
        String networkTEST = ";225.16.16.2";
        String daemonTEST = "10.50.10.72:7500";
        String OCTESTsvr = "CHOT.G86.OCMES.PROD.PEMsvr";
        String ACFTESTsvr = "CHOT.G86.ACFMES.PROD.PEMsvr";
        String TESTsvr = "CHOT.G86.FMES.PROD.PEMsvr";
        String messageNameTEST = "CheckRecipeParameterRequest";

        //启动主备机制，如果启动时报错，就切换到备用机
        ArrayList<String[]> serverList = new ArrayList<>();
        serverList.add(new String[]{service, network + "22", daemon});
        serverList.add(new String[]{serviceTEST, networkTEST, daemonTEST});
        xmlReadStr.rvListenerGroupInit("PEMsvr", messageNameTEST, serverList, false,
                OCTESTsvr, ACFTESTsvr, TESTsvr);

        //测试其他服务器
        String service2 = "7500";
        String network2 = ";225.1.1.1";
        String daemon2 = "10.56.200.238:7500";
        String subject2 = "DEMO.Demosvr";

        xmlReadStr.rvListenerInit(null, service2, network2, daemon2, false, subject2);
        xmlReadStr.start();
    }

}
