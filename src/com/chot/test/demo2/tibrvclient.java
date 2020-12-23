package com.chot.test.demo2;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.tibco.tibrv.*;

public class tibrvclient implements TibrvMsgCallback, TibrvTimerCallback {

    String service = "7522:7524";       /* Two-part service parameter for
                                           direct communication.  To use
                                           ephemeral ports, specify in
                                           the form "7522:" */
    String network = null;
    String daemon = null;
    double interval = 0;                // Default request interval (sec).
    int status_frq = 0;                 // Default frq of status display.
    long requests = 10000;              // Default number of requests.
    static long sent = 0;
    static long responses = 0;
    static String query_subject = "TIBRV.LOCATE";   // To find the server
    static String response_subject;
    static double query_timeout = 10.0;
    static double test_timeout = 10.0;

    TibrvTransport transport;
    TibrvTimer timer;
    static TibrvDate start_dt;
    static TibrvDate stop_dt;
    static double start_time;
    static double stop_time;
    double elapsed;
    long x = 1;
    long y = 2;
    Random rand;

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
        // Parse arguments for possible optional
        // parameters. These must precede the number of requests.
        int i = get_InitParams(args);

        // Create an RVD transport.
        try {
            System.out.println("Create a transport on" +
                    " service " + ((service != null) ? service : "(default)") +
                    " network " + ((network != null) ? network : "(default)") +
                    " daemon " + ((daemon != null) ? daemon : "(default)"));
            transport = new TibrvRvdTransport(service, network, daemon);
            transport.setDescription("tibrvclient");
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
        try {
            query_subject = args[i];
            response_subject = transport.createInbox();
            new TibrvListener(response_queue,
                    this, transport, response_subject, null);
        } catch (TibrvException e) {
            System.err.println("Failed to create listener:");
            e.printStackTrace();
            System.exit(0);
        }

        // Create a message for the query.
        TibrvMsg query_msg = new TibrvMsg();
        try {
            query_msg.setSendSubject(query_subject);
        } catch (TibrvException e) {
            System.err.println("Failed to set send subject:");
            e.printStackTrace();
            System.exit(0);
        }

        // Query for our server.  sendRequest generates an inbox
        // reply subject.
        System.err.println("Attempting to contact server using subject " +
                query_subject + "...");

        TibrvMsg reply_msg = null;
        try {
            reply_msg = transport.sendRequest(query_msg, query_timeout);
        } catch (TibrvException e) {
            System.err.println("Failed to detect server:");
            e.printStackTrace();
            System.exit(0);
        }

        // If timeout, reply message is null and query failed.
        if (reply_msg == null) {
            System.err.println("Failed to detect server.");
            System.exit(0);
        }

        // Report finding a server.
        TibrvMsg server_msg = new TibrvMsg();
        String server_subject = reply_msg.getReplySubject();
        System.out.println("tibrvclient successfully located a server: " +
                server_subject);

        // Create a dispatcher with 5 second timeout to process server replies
        TibrvDispatcher dispatcher = new TibrvDispatcher("Dispatcher", response_queue, 5.0);

        // Set up client request message and report subjects used.  Send subject
        // is the reply subject from the server's answer to our query.
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

        // Initialize the random number generator.  Save the start time.
        rand = new Random();
        System.err.println("Starting test....");
        start_dt = new TibrvDate(new Date());
        start_time = start_dt.getTimeSeconds() + start_dt.getTimeNanoseconds() / 1000000000.0;

        if (interval <= 0) {
            // If interval is 0, loop here to send the client request messages.
            for (i = 0; i < requests; i++) {
                try {
                    server_msg.updateU32("x", (int) rand.nextInt());
                    server_msg.updateU32("y", (int) rand.nextInt());
                } catch (TibrvException e) {
                    System.err.println("Failed to set fields in test message:");
                    e.printStackTrace();
                    System.exit(0);
                }
                try {
                    transport.send(server_msg);
                    sent++;
                    if (status_frq > 0) {
                        if (((sent) % status_frq) == 0) {
                            System.out.println((new Date()).toString() +
                                    ": " + (sent) + " client requests sent");
                        }
                    }
                } catch (TibrvException e) {
                    System.err.println("Failed to send test message:");
                    e.printStackTrace();
                    System.exit(0);
                }
            }
            // We are done sending, so report the number of requests sent and
            // responses received, then destroy the timer.
            System.out.println(responses + " received while sending " +
                    sent + " requests.");
        } else {
            // If the interval is > 0, create a timer with this interval.  A
            // client request message is sent in the timer callback.
            try {
                timer = new TibrvTimer(response_queue, this, interval,
                        server_msg);
            } catch (TibrvException e) {
                System.err.println("Failed to create timer:");
                e.printStackTrace();
                System.exit(0);
            }
        }
    }


    // Listener callback counts responses, reports after all replies received.
    public void onMsg(TibrvListener listener, TibrvMsg msg) {
        responses++;
        if (status_frq > 0) {
            if ((responses % status_frq) == 0) {
                System.out.println((new Date()).toString() +
                        ": " + responses + " server responses received");
            }
        }

        if (responses >= requests) {
            stop_dt = new TibrvDate(new Date());
            stop_time = stop_dt.getTimeSeconds() + stop_dt.getTimeNanoseconds() / 1000000000.0;

            elapsed = stop_time - start_time;

            System.out.println("Client received all " + requests + " responses");
            System.out.println(requests + " requests took " +
                    ((int) (elapsed * 100)) / 100. + " secs to process.");
            System.out.println("Effective rate of " +
                    ((int) (10 * requests / elapsed)) / 10. + " request/sec.");

            transport.destroy();
            System.exit(1);
        }
    }

    // Callback for timer.  Send a request message each time the timer fires.
    public void onTimer(TibrvTimer timer) {
        TibrvMsg server_msg = (TibrvMsg) timer.getClosure();
        if (sent < requests) {
            try {
                server_msg.updateU32("x", (int) rand.nextInt());
                server_msg.updateU32("y", (int) rand.nextInt());
            } catch (TibrvException e) {
                System.err.println("Failed to set fields in test message:");
                e.printStackTrace();
                System.exit(0);
            }
            try {
                transport.send(server_msg);
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
        if (sent == requests) {
            System.out.println(responses + " received while sending " +
                    sent + " requests.");
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
            } else if (args[i].equals("-interval")) {
                interval = Double.parseDouble(args[i + 1]);
                i += 2;
            } else
                usage();
        }
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
