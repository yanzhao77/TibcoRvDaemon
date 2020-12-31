package com.chot.test.inbox;

import com.tibco.tibrv.*;

import java.util.Date;

public class RvlistenerInbox implements TibrvMsgCallback {


    public RvlistenerInbox() {
        // open Tibrv in native implementation
        try {
            Tibrv.open(Tibrv.IMPL_NATIVE);
        } catch (TibrvException e) {
            System.err.println("Failed to open Tibrv in native implementation:");
            e.printStackTrace();
            System.exit(0);
        }
    }


    /**
     * 启动监听
     */
    public void start(String service, String network, String daemon, String subjectName) {
        TibrvQueue tibrvQueue = Tibrv.defaultQueue();
        String inbox_name = null;
        TibrvTransport transport = null;
        try {
            transport = new TibrvRvdTransport(service, network, daemon);
            inbox_name = transport.createInbox();
        } catch (TibrvException e) {
            e.printStackTrace();
        }
        // Create listeners for specified subjects
        // create listener using default queue
        try {
            new TibrvListener(tibrvQueue, this, transport, inbox_name, null);
            System.err.println("Listening on: " + subjectName);

            // Create a message for the query.
            TibrvMsg query_msg = new TibrvMsg();
            try {
                query_msg.setSendSubject(subjectName);
                query_msg.setReplySubject(inbox_name);
                query_msg.update("str", "hello world");
                System.out.println("给server发送消息");
                transport.send(query_msg);
            } catch (TibrvException e) {
                System.err.println("Failed to set send subject:");
                e.printStackTrace();
                System.exit(0);
            }

        } catch (TibrvException e) {
            e.printStackTrace();
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

    public void onMsg(TibrvListener listener, TibrvMsg msg) {
        System.out.println("收到server返回的消息");
        System.out.println((new Date()).toString() +
                ": subject=" + msg.getSendSubject() +
                ", reply=" + msg.getReplySubject()
                + ", message=" + msg.toString()
        );
        System.out.println("ReplySubject:\t" + msg.getReplySubject());
        System.out.flush();
    }

    public static void main(String[] args) {
        String service = "7500";
        String network = ";225.1.1.1";
        String daemons = "10.56.14.176:7500";
        String subjectName = "DEMO.inboxListener";

        RvlistenerInbox rvlistener = new RvlistenerInbox();
        rvlistener.start(service, network, daemons, subjectName);
    }

}
