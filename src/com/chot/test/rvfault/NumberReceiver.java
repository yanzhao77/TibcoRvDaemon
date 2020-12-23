package com.chot.test.rvfault;

import com.tibco.tibrv.Tibrv;
import com.tibco.tibrv.TibrvException;
import com.tibco.tibrv.TibrvFtMember;
import com.tibco.tibrv.TibrvFtMemberCallback;
import com.tibco.tibrv.TibrvListener;
import com.tibco.tibrv.TibrvMsg;
import com.tibco.tibrv.TibrvMsgCallback;
import com.tibco.tibrv.TibrvRvdTransport;

public class NumberReceiver implements TibrvMsgCallback, TibrvFtMemberCallback, Runnable {
    private String service = "7500";
    private String network = ";225.1.1.1";
    private String daemon = "tcp:7500";
    private String subject = "DEMO.FT.NUM";//主

    private String ftservice = "7504";//备
    private String ftnetwork = ";225.1.10.1";
    private String ftdaemon = "tcp:7504";

    private String ftgroupName = "DEMO.FT.GROUP";//daemon组，同一个group name的程序为一个群组，互相备份
    private int ftweight = 10;//权重，由 1 到整數的最大值，數字越大優先權越大
    private int activeGoalNum = 1;//實務上要有幾支程式處於 ACTIVATE 狀態是可以設定的，這個數字稱為 active goal
    private double hbInterval = 1.5;//心跳时间间隔
    private double prepareInterval = 3;//准备时间间隔
    private double activateInterval = 4.8;//激活间隔

    private TibrvRvdTransport transport = null;//rv对象
    private TibrvListener listener = null;//监听

    private boolean active = false;//备份机组的状态 系统状态
    //DEACTIVATE: 顧名思義，就是非屬於提供服務的狀態，這時候系統當然就別做什麼事，只要一直傾聽 RV 的訊息，當傾聽到要切換狀態時再進行相關工作。
    //PREPARE_TO_ACTIVATE: 當 RV 送來這個狀態，表示原本正提供服務的系統可能出了狀況，備援的系統要開始準備啟動，通常收到這個 event 時，會進行一些系統資源初始化的工作，以便當程式真的要啟動提供服務時，可以儘快啟動，以使服務中斷的時間儘可能的縮短。
    //ACTIVATE: 收到這個 event，就真的要接手服務了!
    //上述三個狀態的切換，RV 會透過 onFtAction 這個 callback function 通知備援的程式，所以程式要實作 TibrvFtMemberCallback 這個介面。


    @Override
    public void run() {
        try {
            Tibrv.open(Tibrv.IMPL_NATIVE);//开启rv
            transport = new TibrvRvdTransport(service, network, daemon);//链接到指定daemon
            transport.setDescription("1号机");
            TibrvRvdTransport fttransport = new TibrvRvdTransport(ftservice, ftnetwork, ftdaemon);//创建备用机组
            fttransport.setDescription("fault tolerance");//添加说明

            new TibrvFtMember(Tibrv.defaultQueue(), // TibrvQueue
                    this,                 // TibrvFtMemberCallback
                    fttransport,          // TibrvTransport
                    ftgroupName,          // groupName 组名称
                    ftweight,             // 权重
                    activeGoalNum,        // activeGoal
                    hbInterval,           // 心跳时间间隔
                    prepareInterval,      // 准备时间间隔,
                    // Zero is a special value,零是一个特殊值
                    // indicating that the member does 指示成员不需要预先警告即可激活
                    // not need advance warning to activate
                    activateInterval,     // activationInterval 激活间隔
                    null);                // closure 关闭


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
            listener = new TibrvListener(Tibrv.defaultQueue(), this, transport, subject, null);
            System.out.println("Start Listening on: " + subject);
        } catch (TibrvException e) {
            System.err.println("Failed to create subject listener:");
            System.exit(0);
        }
    }

    void disableListener() {
        listener.destroy();
        System.out.println("Destroy Listener on Subject: " + subject);
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

    }

    public static void main(String[] args) throws InterruptedException {
        NumberReceiver rcv = new NumberReceiver();
        Thread tRcv = new Thread(rcv);
        tRcv.start();
        tRcv.join();
        System.out.println("stop");
    }
}