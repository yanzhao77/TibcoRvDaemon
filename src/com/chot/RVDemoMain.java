package com.chot;


import com.chot.entity.daesonEntity.TibrvRvdTransportParameter;
import com.chot.service.XmlForObjectService;
import com.chot.service.serviceImpl.XmlForObjectServiceImpl;

import java.util.ArrayList;
import java.util.List;


public class RVDemoMain {

    public static void main(String[] args) throws ClassNotFoundException {
        XmlForObjectService xmlReadStr = new XmlForObjectServiceImpl();
        String messageName = "GetOicMainLotList";// 要拦截的message的名称
        // prod监听
//        String service = "8400";
//        String network = ";225.16.16.4";
//        String daemon = "tcp:10.50.10.66:7500";
//        String ACFCNMsvr = "CHOT.G86.ACFMES.PROD.CNMsvr";

//        String serviceTEST = "8410";
//        String networkTEST = ";225.9.9.4";
//        String daemonTEST = "10.50.10.72:7500";
//        String ACFTESTsvr = "CHOT.G86.ACFMES.TEST.CNMsvr";

        String service = "7500";
        String network = ";225.1.1.1";
        String daemons = "10.56.14.176:7500";
        String subject = "DEMO.Demosvr";

        String service2 = "1000";
        String network2 = ";225.1.1.1";
        String daemon2 = "10.56.200.238:7500";
        String subject2 = "DEMO.Demosvr";


        //启动主备机制，如果启动时报错，就切换到备用机
        List<TibrvRvdTransportParameter> rvdTransportList = new ArrayList<>();
        rvdTransportList.add(new TibrvRvdTransportParameter(service2, network2, daemon2, 1));
        rvdTransportList.add(new TibrvRvdTransportParameter(service, network, daemons, 1));
        xmlReadStr.rvListenerObjGroupInit("CNMsvr", messageName, rvdTransportList, false, subject2);

        //测试其他服务器
//        String service2 = "7500";
//        String network2 = ";225.1.1.1";
//        String daemon2 = "10.56.200.238:7500";
//        String subject2 = "DEMO.Demosvr";

//        xmlReadStr.rvListenerInit(null, service2, network2, daemon2, false, subject2);


        xmlReadStr.start();
    }

}
