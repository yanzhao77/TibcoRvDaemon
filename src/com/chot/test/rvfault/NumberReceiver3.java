package com.chot.test.rvfault;

import com.chot.messageCheck.MessageReadCallback;
import com.chot.rvDaesonGroup.TibrvRvdTransportParameter;
import com.tibco.tibrv.*;

public class NumberReceiver3 implements TibrvMsgCallback, TibrvFtMemberCallback {

    MessageReadCallback messageRead;
    private TibrvRvdTransport transport = null;//rv对象
    private TibrvListener listener = null;//监听

    private boolean active = false;//备份机组的状态 系统状态

    public NumberReceiver3() {
        try {
            Tibrv.open(Tibrv.IMPL_NATIVE);//开启rv
        } catch (TibrvException e) {
            e.printStackTrace();
        }
    }

    public void setTransports(TibrvRvdTransportParameter rvdTransport, TibrvRvdTransportParameter... tibrvRvdTransportParameter) {
        try {
            Tibrv.open(Tibrv.IMPL_NATIVE);//开启rv
            transport = rvdTransport;//链接到指定daemon
            for (TibrvRvdTransportParameter fttransport : tibrvRvdTransportParameter) {
                new TibrvFtMember(Tibrv.defaultQueue(), // TibrvQueue
                        this,                 // TibrvFtMemberCallback
                        fttransport,          // TibrvTransport
                        fttransport.getGroupName(),          // groupName 组名称
                        fttransport.getFtWeight(),             // 权重
                        fttransport.getActiveGoalNum(),        // activeGoal
                        fttransport.getHbInterval(),           // 心跳时间间隔
                        fttransport.getPrepareInterval(),      // 准备时间间隔,
                        fttransport.getActivateInterval(),     // activationInterval 激活间隔
                        null);                // closure 关闭

            }

            while (true) {
                try {
                    Tibrv.defaultQueue().dispatch();
                } catch (TibrvException e) {
                    System.err.println("Exception dispatching default queue:");
                    System.exit(0);
                } catch (InterruptedException ie) {
                    System.exit(0);
                }
            }
        } catch (TibrvException e) {
            e.printStackTrace();
        }
    }

    void enableListener() {
        try {
            // Subscribe to subject
            if (transport instanceof TibrvRvdTransportParameter) {
                TibrvRvdTransportParameter transportParameter = (TibrvRvdTransportParameter) transport;
                for (String subject : ((TibrvRvdTransportParameter) transport).getSubject()) {
                    new TibrvListener(Tibrv.defaultQueue(), this, transportParameter, subject, null);
                }
            }
//            System.out.println("Start Listening on: " + subject);
        } catch (TibrvException e) {
            System.err.println("Failed to create subject listener:");
            System.exit(0);
        }
    }

    void disableListener() {
//        listener.destroy();
//        System.out.println("Destroy Listener on Subject: " + subject);
    }

    @Override
    public void onFtAction(TibrvFtMember member, String ftgroupName, int action) {
        if (action == TibrvFtMember.PREPARE_TO_ACTIVATE) {
            System.out.println("TibrvFtMember.PREPARE_TO_ACTIVATE invoked...");
            System.out.println("*** PREPARE TO ACTIVATE: " + ftgroupName);
        } else if (action == TibrvFtMember.ACTIVATE) {
            System.out.println("TibrvFtMember.ACTIVATE invoked...");
            System.out.println("*** ACTIVATE: " + ftgroupName);
            enableListener();
            active = true;
        } else if (action == TibrvFtMember.DEACTIVATE) {
            System.out.println("TibrvFtMember.DEACTIVATE invoked...");
            System.out.println("*** DEACTIVATE: " + ftgroupName);
            disableListener();
            active = false;
        }
    }

    @Override
    public void onMsg(TibrvListener listener, TibrvMsg msg) {
        try {
            System.out.println(listener.getSubject());
            System.out.println(listener.getTransport().getDescription());
            int num = msg.getAsInt("number", 0);
            System.out.println("number: " + num);
        } catch (TibrvException e) {
            e.printStackTrace();
        }
//        messageRead.readMessage(msg);
    }

    public static void main(String[] args) throws InterruptedException, TibrvException {
        NumberReceiver3 rcv = new NumberReceiver3();

        String service = "7500";
        String network = ";225.1.1.1";
        String daemon = "tcp:7500";
        String subject = "DEMO.FT.NUM";//主

        String ftservice = "7504";//备
        String ftnetwork = ";225.1.10.1";
        String ftdaemon = "tcp:7504";

        TibrvRvdTransportParameter t1 = new TibrvRvdTransportParameter("7500", ";225.1.1.1",
                "tcp:7500", new String[]{"DEMO.FT.NUM"});
        t1.setFaultToleranceParameter("DEMO.FT.GROUP", "3号机");
        TibrvRvdTransportParameter t2 = new TibrvRvdTransportParameter("7504", ";225.1.10.1",
                "tcp:7504", new String[]{"DEMO.FT.NUM"});
        t2.setFaultToleranceParameter("DEMO.FT.GROUP", "4号机");
        rcv.setTransports(t1, t2);
        System.out.println("stop");
    }
}