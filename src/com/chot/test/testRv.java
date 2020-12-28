package com.chot.test;

import com.chot.messageCheck.MessageReadCallback;
import com.tibco.tibrv.*;

import java.util.ArrayList;
import java.util.List;

/**
 * @version 1.0
 * @Classname test
 * @Description TODO
 * @Date 2020/12/28 16:46
 * @Created by yan34177
 */
public class testRv {
    MessageReadCallback messageReadCallback;

    public testRv() {
        // open Tibrv in native implementation
        try {
            Tibrv.open(Tibrv.IMPL_NATIVE);
        } catch (TibrvException e) {
            System.err.println("Failed to open Tibrv in native implementation:");
            e.printStackTrace();
            System.exit(0);
        }
        messageReadCallback = new MessageReadCallback() {
            @Override
            public void onMsg(TibrvListener tibrvListener, TibrvMsg tibrvMsg) {
                System.out.println(tibrvMsg.getSendSubject());
            }
        };

    }


    /**
     * 启动监听
     */
    public void start(List<String[]> serverList, String subjectName0, String subjectName1) {
        TibrvQueue tibrvQueue = null;
        try {
            tibrvQueue = new TibrvQueue();
        } catch (TibrvException e) {
            e.printStackTrace();
        }
        for (int i = 0; i < serverList.size(); i++) {
            String[] strings = serverList.get(i);
            String service = strings[0];
            String network = strings[1];
            String daemon = strings[2];
            String subject = strings[3];


            TibrvTransport transport = null;
            try {
                transport = new TibrvRvdTransport(service, network, daemon);
            } catch (TibrvException e) {
                //如果这个备份机不可用，就启动其他的，
                System.err.println(e.getMessage());
            }

            try {
                new TibrvListener(tibrvQueue, messageReadCallback, transport,
                        subject, null);
                System.err.println("Listening on: " + strings[3]);
            } catch (TibrvException e) {
                System.err.println("Failed to create listener:");
                e.printStackTrace();
                System.exit(0);
            }
        }

//
//        String service = "8400";
//        String network = ";225.16.16.4";
//        String daemon = "tcp:10.50.10.66:7500";
//        String subjectACFCNMsvr = "CHOT.G86.ACFMES.PROD.CNMsvr";
//
//        TibrvTransport transport = null;
//        try {
//            transport = new TibrvRvdTransport(service, network, daemon);
//        } catch (TibrvException e) {
//            //如果这个备份机不可用，就启动其他的，
//        }
//
//        try {
//            new TibrvListener(tibrvQueue, messageReadCallback, transport,
//                    subjectACFCNMsvr, null);
//            System.err.println("Listening on: " + subjectACFCNMsvr);
//        } catch (TibrvException e) {
//            System.err.println("Failed to create listener:");
//            e.printStackTrace();
//            System.exit(0);
//        }
//
//        String serviceTEST = "7500";
//        String networkTEST = ";225.1.1.1";
//        String daemonTEST = "tcp:7500";
//        String subjectACFTESTsvr = "DEMO.FT.NUM";
//        TibrvTransport transport2 = null;
//        try {
//            transport2 = new TibrvRvdTransport(serviceTEST, networkTEST, daemonTEST);
//        } catch (TibrvException e) {
//            //如果这个备份机不可用，就启动其他的，
//            e.getMessage();
//        }
//
//        try {
//            new TibrvListener(tibrvQueue, messageReadCallback, transport2,
//                    subjectACFTESTsvr, null);
//            System.err.println("Listening on: " + subjectACFTESTsvr);
//        } catch (TibrvException e) {
//            System.err.println("Failed to create listener:");
//            e.printStackTrace();
//            System.exit(0);
//        }


        // dispatch Tibrv events
        startt(tibrvQueue);
    }

    public void startt(TibrvQueue tibrvQueue) {
        while (true) {
            try {
                tibrvQueue.dispatch();
            } catch (TibrvException e) {
                System.err.println("Exception dispatching default queue:");
                e.printStackTrace();
                System.exit(0);
            } catch (InterruptedException ie) {
                System.exit(0);
            }
        }
    }

    public static void main(String[] args) {
        String service = "8400";
        String network = ";225.16.16.4";
        String daemon = "tcp:10.50.10.66:7500";
        String subjectACFCNMsvr = "CHOT.G86.ACFMES.PROD.CNMsvr";

        String serviceTEST = "8210";
        String networkTEST = ";225.9.9.2";
        String daemonTEST = "10.50.10.72:7500";
        String subjectACFTESTsvr = "CHOT.G86.ACFMES.PROD.PEMsvr";

        String service2 = "7500";
        String network2 = ";225.1.1.1";
        String daemon2 = "tcp:7500";
        String subject2 = "DEMO.FT.NUM";

        List<String[]> stringList = new ArrayList<>();
        stringList.add(new String[]{service, network, daemon, subjectACFCNMsvr});
        stringList.add(new String[]{service2, network2, daemon2, subject2});
        testRv testRv = new testRv();
        testRv.start(stringList, subjectACFCNMsvr, subject2);
    }
}
