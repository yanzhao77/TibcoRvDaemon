package com.chot.test.rvTest;

import java.util.*;

import com.tibco.tibrv.*;

public class tibrvlisten implements TibrvMsgCallback {

    String service = null;
    String network = null;
    String daemon = null;

    public tibrvlisten(String args[]) {
        // parse arguments for possible optional
        // parameters. These must precede the subject
        // and message strings
        int i = get_InitParams(args);

        // we must have at least one subject
        if (i >= args.length)
            usage();

        // open Tibrv in native implementation
        try {
            Tibrv.open(Tibrv.IMPL_NATIVE);
        } catch (TibrvException e) {
            System.err.println("Failed to open Tibrv in native implementation:");
            e.printStackTrace();
            System.exit(0);
        }

        // Create RVD transport
        TibrvTransport transport = null;
        try {
            transport = new TibrvRvdTransport(service, network, daemon);
        } catch (TibrvException e) {
            System.err.println("Failed to create TibrvRvdTransport:");
            e.printStackTrace();
            System.exit(0);
        }

        // Create listeners for specified subjects
        while (i < args.length) {
            // create listener using default queue
            try {
                new TibrvListener(Tibrv.defaultQueue(),
                        this, transport, args[i], null);
                System.err.println("Listening on: " + args[i]);
            } catch (TibrvException e) {
                System.err.println("Failed to create listener:");
                e.printStackTrace();
                System.exit(0);
            }
            i++;
        }

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

    public void onMsg(TibrvListener listener, TibrvMsg msg) {
        System.out.println((new Date()).toString() +
                ": subject=" + msg.getSendSubject() +
                ", reply=" + msg.getReplySubject() +
                ", message=" + msg.toString()
        );
        System.out.flush();
    }

    // print usage information and quit
    void usage() {
        System.err.println("Usage: java tibrvlisten [-service service] [-network network]");
        System.err.println("            [-daemon daemon] <subject-list>");
        System.exit(-1);
    }

    int get_InitParams(String[] args) {
        int i = 0;
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
            } else
                usage();
        }
        return i;
    }

    public static void main(String args[]) {
        new tibrvlisten(args);
    }

}
