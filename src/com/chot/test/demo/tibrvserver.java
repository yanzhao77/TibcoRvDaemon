package com.chot.test.demo;


import com.tibco.tibrv.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

public class tibrvserver implements TibrvMsgCallback /*, TibrvTimerCallback */ {
    String service = "7522:7523";   /* Two-part service parameter for direct
                                       communication.  To use ephemeral port
                                       specify in the form "7522:"  */
    String network = null;
    String daemon = null;
    long status_frq = 0;            /* Default frequency 0 for no status
                                       display while sending and receiving. */
    long requests = 0;
    double server_timeout = 120; //发送消息等待时间，如果在此间隔内未收到//消息，则退出。

    static String request_subject;
    static String query_subject;

    TibrvTransport transport;
    TibrvTimer timer;
    TibrvMsg reply_msg;
    TibrvMsg response_msg;
    boolean startInbox;//开启inbox

    int x;
    int y;
    int sum;

    boolean msg_received = true;//等待接收消息
    boolean event_dispatched;//接收超时

    public tibrvserver() {
    }

    public void init(String... args) {
        // parse arguments for possible optional
        // parameters.
        args = addServerName(args);
        int i = get_InitParams(args);

        // open Tibrv in native implementation
        try {
            Tibrv.open(Tibrv.IMPL_NATIVE);
            System.out.println((new Date()).toString() +
                    ": tibrvserver (TIBCO Rendezvous V" +
                    Tibrv.getVersion() + " Java API)");
        } catch (TibrvException e) {
            System.err.println("Failed to open Tibrv in native implementation:");
            e.printStackTrace();
            System.exit(0);
        }

        // Create a transport.
        try {
            transport = new TibrvRvdTransport(service, network, daemon);
            transport.setDescription("tibrvserver");
        } catch (TibrvException e) {
            System.err.println("Failed to create TibrvRvdTransport:");
            e.printStackTrace();
            System.exit(0);
        }

        // inbox返回消息监听
        try {
            request_subject = transport.createInbox();

            new TibrvListener(Tibrv.defaultQueue(),
                    this, transport, request_subject, null);
        } catch (TibrvException e) {
            System.err.println("Failed to initialilze request listener:");
            e.printStackTrace();
            System.exit(0);
        }

        // 主消息名称监听
        try {
            new TibrvListener(Tibrv.defaultQueue(),
                    this, transport, query_subject, null);
        } catch (TibrvException e) {
            System.err.println("Failed to initialilze query listener:");
            e.printStackTrace();
            System.exit(0);
        }

        // create query reply and request response messages 创建查询-答复和请求-响应消息
        reply_msg = new TibrvMsg();
        response_msg = new TibrvMsg();

        // Display a server-ready message.
        System.out.println("Listening for client requests on subject " +
                request_subject + "\n" +
                "Wait time is " + server_timeout + " secs\n" +
                (new Date()).toString() + ": tibrvserver ready...");

        // dispatch Tibrv events with <server_timeout> second timeout.  If
        // message not received within this interval, quit.
        //使用<server_timeout>second timeout分派Tibrv事件。如果在此间隔内未收到//消息，则退出。
        while (msg_received) {
            msg_received = false;
            try {
                event_dispatched = Tibrv.defaultQueue().timedDispatch(server_timeout);
            } catch (TibrvException e) {
                System.err.println("Exception dispatching default queue:");
                e.printStackTrace();
                System.exit(0);
            } catch (InterruptedException ie) {
                System.exit(0);
            }
        }
        if (!event_dispatched)
            System.err.println("tibrvserver: timedDiapatch received timeout");
        System.out.println((new Date()).toString() +
                ": " + requests + " client requests processed");

    }

    // Message callback.  Flag message received.  If query, reply with server's
    // request subject.  If request, validate message and reply.
    // 消息回调。收到标志消息。如果查询，用服务器的//请求主题进行答复。如果请求，验证消息并回复。
    public void onMsg(TibrvListener listener, TibrvMsg msg) {
        msg_received = true;
        if (listener.getSubject().equals(query_subject)) {
            try {
                reply_msg.setReplySubject(request_subject);
                transport.sendReply(reply_msg, msg);
                System.out.println((new Date()).toString() +
                        ": Client search message received");
            } catch (TibrvException e) {
                System.err.println("Exception dispatching default queue:");
                e.printStackTrace();
                System.exit(0);
            }

            try {
                for (int i = 1; i <= 1000; i++) {
                    response_msg.update("number", i, TibrvMsg.U32);
                    System.out.println("number" + i);
                    transport.sendReply(response_msg, msg);
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                    }
                }

            } catch (TibrvException e) {
                System.err.println("Error sending a response to request message:");
                e.printStackTrace();
                return;
            }
        }
    }

    // print usage information and quit
    void usage() {
        System.err.println("Usage: java tibrvserver [-service <service>] [-network <network>]");
        System.err.println("                        [-daemon  <daemon>]  [-status  <#msgs>]");
        System.exit(-1);
    }

    // parse command line parameters.
    int get_InitParams(String[] args) {
        int i = 0;
        System.out.println(Arrays.toString(args));
        if (args.length > 0) {
            if (args[i].equals("-?") ||
                    args[i].equals("-h") ||
                    args[i].equals("-help")) {
                usage();
            }
        }
        while (i < args.length - 1 && args[i].startsWith("-")) {
            if (args[i].equals("-service")) {
                service = args[i + 1];
                i += 2;
            } else if (args[i].equals("-network")) {
                network = args[i + 1];
                i += 2;
            } else if (args[i].equals("-daemon")) {
                daemon = args[i + 1];
                i += 2;
            } else if (args[i].equals("-status")) {
                status_frq = Integer.parseInt(args[i + 1]);
                i += 2;
            } else
                usage();
        }
        query_subject = args[args.length - 1];
        return i;
    }

    /**
     * 添加命令参数
     *
     * @param args
     * @return
     */
    public String[] addServerName(String[] args) {
        List<String> stringList = new ArrayList<>();
        for (int i = 0; i < args.length; i++) {
            String serverName = "";
            if (i == 0) {
                serverName = "-service";
            } else if (i == 1) {
                serverName = "-network";
            } else if (i == 2) {
                serverName = "-daemon";
            }
            if (!serverName.equals("")) {
                stringList.add(serverName);
            }
            stringList.add(args[i]);
        }
        return stringList.toArray(new String[stringList.size()]);
    }


    public static void main(String args[]) {
        String service = "8210";
        String network = ";225.9.9.2";
        String daemon = "127.0.0.1:7500";
        String subject = "CHOT.G86.MES.TEST.PEMsvr2";
        tibrvserver tibrvserver = new tibrvserver();
        tibrvserver.init(service, network, daemon, subject);
    }

}
