package com.chot.test.demo;

import com.tibco.tibrv.*;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class tibrvclient implements TibrvMsgCallback, TibrvTimerCallback {

    String service = "7522:7524";       /* Two-part service parameter for
                                           direct communication.  To use
                                           ephemeral ports, specify in
                                           the form "7522:" */
    String network = null;
    String daemon = null;
    double interval = 0;                // Default request interval (sec).默认请求间隔（秒）。
    int status_frq = 0;                 // Default frq of status display.状态显示的默认frq。
    long requests = 10000;              // Default number of requests.默认请求数。
    static long sent = 0;
    //    static long responses = 0;      //响应次数
    static String query_subject;   // To find the server 服务器
    static String response_subject;     //inbox 名称
    static double query_timeout = 10.0;
    static double test_timeout = 10.0;

    TibrvTransport transport;
    TibrvTimer timer;
    static TibrvDate start_dt;
    static TibrvDate stop_dt;
    static double start_time;
    static double stop_time;
    double elapsed;

    boolean startInbox; //开启inbox


    public tibrvclient() {
        // open Tibrv in native implementation.
        try {

            Tibrv.open(Tibrv.IMPL_NATIVE);
            System.out.println((new Date()).toString() +
                    ": tibrvclient (TIBCO Rendezvous V" +
                    Tibrv.getVersion() + " Java API)");
        } catch (TibrvException e) {
            System.err.println("Failed to open Tibrv in native implementation:");
            e.printStackTrace();
            System.exit(0);
        }
    }

    public void init(String... args) {
        args = addServerName(args);
        int i = get_InitParams(args);

        // if requests value is given, set requests coun.t 如果给定了请求值，则设置请求计数
//        if (args.length > i) {
//            requests = Integer.parseInt(args[i]);
//        }


        // Create an RVD transport.
        try {
            transport = new TibrvRvdTransport(service, network, daemon);
            transport.setDescription("tibrvclient");//说明
        } catch (TibrvException e) {
            System.err.println("Failed to create TibrvRvdTransport:");
            e.printStackTrace();
            System.err.println(" ");
            System.exit(0);
        }

        // Create a response queue
        TibrvQueue response_queue = null;
        try {
            response_queue = new TibrvQueue();
        } catch (TibrvException e) {
            System.err.println("Failed to create TibrvQueue:");
            e.printStackTrace();
            System.exit(0);
        }

        // Create an inbox subject for communication with the server and
        // create a listener for this response subject.
        //创建与服务器通信的收件箱主题，并为此响应主题创建侦听器。
        try {
            response_subject = transport.createInbox();
            new TibrvListener(response_queue,//创建inbox监听
                    this, transport, response_subject, null);
        } catch (TibrvException e) {
            System.err.println("Failed to create listener:");
            e.printStackTrace();
            System.exit(0);
        }

        // Create a message for the query.
        TibrvMsg query_msg = new TibrvMsg();//message消息监听
        try {
            query_msg.setSendSubject(query_subject);
        } catch (TibrvException e) {
            System.err.println("Failed to set send subject:");
            e.printStackTrace();
            System.exit(0);
        }

        TibrvMsg reply_msg = null;
        try {
            //启用资源监听
            reply_msg = transport.sendRequest(query_msg, query_timeout);
        } catch (TibrvException e) {
            System.err.println("Failed to detect server:");
            e.printStackTrace();
            System.exit(0);
        }

        // If timeout, reply message is null and query failed.如果超时，则回复消息为空，查询失败。
        if (reply_msg == null) {
            System.err.println("Failed to detect server.");
            System.exit(0);
        }

        // Report finding a server.报告正在查找服务器。
        TibrvMsg server_msg = new TibrvMsg();
        String server_subject = reply_msg.getReplySubject();
        System.out.println("tibrvclient successfully located a server: " +
                server_subject);


        // Set up client request message and report subjects used.  Send subject
        // is the reply subject from the server's answer to our query.
        //设置客户端请求消息并报告使用的主题。Send subject是服务器对我们的查询的答复主题。
        try {
            System.out.println("Set server subject to : " + server_subject);
            server_msg.setSendSubject(server_subject);
            System.out.println("Set client subject to : " + response_subject);
            server_msg.setReplySubject(response_subject);
        } catch (TibrvException e) {
            System.err.println("Failed to set subjects, fields for test message:");
            e.printStackTrace();
            System.exit(0);
        }

        serviceImp(i, server_msg, response_queue);

        // dispatch Tibrv events
        while (true) {
            try {
                Tibrv.defaultQueue().dispatch();
            } catch (TibrvException e) {
                System.err.println("Exception dispatching default queue:");
                e.printStackTrace();
                System.exit(0);
            } catch (InterruptedException ie) {
                System.exit(0);
            }
        }

    }

    public void serviceImp(int i, TibrvMsg server_msg, TibrvQueue response_queue) {
        Scanner scanner = new Scanner(System.in);
        String str = scanner.nextLine();
        try {
            server_msg.update("message", str);
            transport.send(server_msg);//发送消息，测试是否接收
        } catch (TibrvException e) {
            e.printStackTrace();
        }
    }


    // 侦听器回调统计响应，并在收到所有答复后报告。
    public void onMsg(TibrvListener listener, TibrvMsg msg) {
        System.out.println(msg.getSendSubject());
    }

    // 计时器回调。每次触发计时器时发送请求消息。
    public void onTimer(TibrvTimer timer) {
        TibrvMsg server_msg = (TibrvMsg) timer.getClosure();
        if (sent < requests) {
            try {
                server_msg.update("heartbeat", true);
            } catch (TibrvException e) {
                System.err.println("Failed to set fields in test message:");
                e.printStackTrace();
                System.exit(0);
            }
            try {
                transport.send(server_msg);//发送消息，测试是否接收
            } catch (TibrvException e) {
                System.err.println("Failed to send test message:");
                e.printStackTrace();
                System.exit(0);
            }
            sent++;
            if (status_frq > 0) {
                if (((sent) % status_frq) == 0) {
                    System.out.println((new Date()).toString() +
                            ": " + (sent) + " client requests sent");
                }
            }
        }
        // We are done sending, so report the number of requests sent and
        // responses received, then destroy the timer.
        //发送完毕，请报告发送的请求数和//收到的响应数，然后销毁计时器
        if (sent == requests) {
            System.out.println(" received while sending " + sent + " requests.");
            timer.destroy();
        }
    }

    // Print usage information and quit
    void usage() {
        System.err.println("Usage: java tibrvclient [-service  <service>] [-network <network>]");
        System.err.println("                        [-daemon   <daemon>]  [-status  <#msgs>]");
        System.err.println("                        [-interval <secs>]    [<#requests>]");
        System.exit(-1);
    }

    // Parse command line parameters.
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
        while (i < args.length && args[i].startsWith("-")) {
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
            } else if (args[i].equals("-interval")) {
                interval = Double.parseDouble(args[i + 1]);
                i += 2;
            }
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


    /**
     * 获取指定标签的指定属性的值
     *
     * @param source  要匹配的源文本
     * @param element 标签名称
     * @return 属性值列表
     */
    public static List<String> match(String source, String element) {
        List<String> result = new ArrayList<String>();
        String reg = "<" + element + ">(.*?)</" + element + ">";
        Matcher m = Pattern.compile(reg).matcher(source);
        while (m.find()) {
            String r = m.group(1);
            result.add(r);
        }
        return result;
    }


    public static void main(String args[]) {
        String service = "8210";
        String network = ";225.9.9.2";
        String daemon = "127.0.0.1:7500";
        String subject = "CHOT.G86.MES.TEST.PEMsvr2";
        tibrvclient tibrvclient = new tibrvclient();
        tibrvclient.init(service, network, daemon, subject);
    }

}
