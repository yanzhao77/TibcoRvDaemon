package com.chot.test.demo2;

import java.util.*;

import com.tibco.tibrv.*;

public class tibrvserver implements TibrvMsgCallback /*, TibrvTimerCallback */ {
    String service = "7522:7523";   /* Two-part service parameter for direct
                                       communication.  To use ephemeral port
                                       specify in the form "7522:"  */
    String network = null;
    String daemon = null;
    long status_frq = 0;            /* Default frequency 0 for no status
                                       display while sending and receiving. */
    long requests = 0;
    double server_timeout = 120;

    static String request_subject;
    static String query_subject = "TIBRV.LOCATE";

    TibrvTransport transport;
    TibrvTimer timer;
    TibrvMsg reply_msg;
    TibrvMsg response_msg;

    int x;
    int y;
    int sum;

    boolean msg_received = true;
    boolean event_dispatched;


    public tibrvserver() {
    }

    public void init(String... args) {
        // parse arguments for possible optional
        // parameters.
        args = addServerName(args);
        int i = get_InitParams(args);

        // open Tibrv in native implementation
        try {
	    /*
	      When using IPM, there are 3 ways to provide configuration parameters:
	      1) Using the new Tibrv.setRVParameters API.
	      2) Calling Open with the pathname of a configuration file.
	      3) Placing a "tibrvipm.cfg" configuration file somewhere in PATH.

	      Uncomment the following line to test approach 2):
	      Tibrv.open(".\\tibrvipm.cfg");

	      NOTE: Add *only* the Rendezvous IPM jar file to your classpath to use IPM.
	    */
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
            System.out.println("Create a transport on" +
                    " service " + ((service != null) ? service : "(default)") +
                    " network " + ((network != null) ? network : "(default)") +
                    " daemon " + ((daemon != null) ? daemon : "(default)"));
            transport = new TibrvRvdTransport(service, network, daemon);
            transport.setDescription("tibrvserver");
        } catch (TibrvException e) {
            System.err.println("Failed to create TibrvRvdTransport:");
            e.printStackTrace();
            System.exit(0);
        }

        // Create request subject (inbox) and listener
        try {
            query_subject = args[i];
            request_subject = transport.createInbox();
            new TibrvListener(Tibrv.defaultQueue(),
                    this, transport, request_subject, null);
        } catch (TibrvException e) {
            System.err.println("Failed to initialilze request listener:");
            e.printStackTrace();
            System.exit(0);
        }

        // Create query listener
        try {
            new TibrvListener(Tibrv.defaultQueue(),
                    this, transport, query_subject, null);
        } catch (TibrvException e) {
            System.err.println("Failed to initialilze query listener:");
            e.printStackTrace();
            System.exit(0);
        }

        // create query reply and request response messages
        reply_msg = new TibrvMsg();
        response_msg = new TibrvMsg();

        // Display a server-ready message.
        System.out.println("Listening for client searches on subject " +
                query_subject + "\n" +
                "Listening for client requests on subject " +
                request_subject + "\n" +
                "Wait time is " + server_timeout + " secs\n" +
                (new Date()).toString() + ": tibrvserver ready...");

        // dispatch Tibrv events with <server_timeout> second timeout.  If
        // message not received within this interval, quit.
        while (msg_received) {
            msg_received = false;
            try {
                event_dispatched =
                        Tibrv.defaultQueue().timedDispatch(server_timeout);
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
        } else {
            try {
                x = msg.getAsInt("x", 0);
            } catch (TibrvException e) {
                System.err.println("tibrvserver: Received bad request (x param).");
                return;
            }
            try {
                y = msg.getAsInt("y", 0);
            } catch (TibrvException e) {
                System.err.println("tibrvserver: Received bad request (y param).");
                return;
            }
            sum = x + y;
            try {
                response_msg.update("sum", sum, TibrvMsg.U32);
                transport.sendReply(response_msg, msg);
                requests++;
                if (status_frq > 0) {
                    if ((requests % status_frq) == 0) {
                        System.out.println((new Date()).toString() +
                                ": " + requests + " client requests processed");

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
