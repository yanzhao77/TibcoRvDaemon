package com.chot.test.inbox;

import com.tibco.tibrv.*;

import java.io.UnsupportedEncodingException;
import java.util.Date;

/**
 * @version 1.0
 * @Classname inboxSend
 * @Description TODO
 * @Date 2020/12/31 10:12
 * @Created by yan34177
 */
public class RVinboxSend implements TibrvMsgCallback {
    private String service = "7500";
    private String network = ";225.1.1.1";
    private String daemons = "10.56.14.176:7500";
    private String subjectName = "DEMO.inboxListener";
    TibrvTransport transport = null;

    public RVinboxSend() {
        try {
            Tibrv.open(Tibrv.IMPL_NATIVE);
        } catch (TibrvException e) {
            e.printStackTrace();
        }
    }

    public void sendForServer(TibrvTransport transport) {
        TibrvMsg tibrvMsg = new TibrvMsg();
        try {
            tibrvMsg.setSendSubject(subjectName);
            TibrvMsg.setStringEncoding("Big5");
            transport.send(tibrvMsg);

        } catch (TibrvException | UnsupportedEncodingException e) {
            e.printStackTrace();
        }

    }

    public void start() {
        TibrvQueue tibrvQueue = null;
        try {
            tibrvQueue = new TibrvQueue();
            transport = new TibrvRvdTransport(service, network, daemons);
            new TibrvListener(tibrvQueue, this, transport, subjectName, null);
            System.err.println("Listening on: " + subjectName);
        } catch (TibrvException e) {
            System.err.println("Failed to create listener:");
            e.printStackTrace();
            System.exit(0);
        }
        // dispatch Tibrv events
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

        RVinboxSend rVinboxSend = new RVinboxSend();
        rVinboxSend.start();
    }

    @Override
    public void onMsg(TibrvListener tibrvListener, TibrvMsg msg) {
        System.out.println((new Date()).toString() +
                ": subject=" + msg.getSendSubject() +
                ", reply=" + msg.getReplySubject()
                + ", message=" + msg.toString()
        );
        System.out.println("收到消息，根据inbox返回");
        if (msg.getReplySubject() != null) {
            try {
                TibrvMsg reply_msg = new TibrvMsg();
                reply_msg.setReplySubject(msg.getReplySubject());
                reply_msg.update("str", "你好，世界");
                transport.sendReply(reply_msg, msg);
            } catch (TibrvException e) {
                e.printStackTrace();
            }
        }
        System.out.flush();
    }
}
