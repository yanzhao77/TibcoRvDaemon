package com.chot.test.rvTest;

import com.tibco.tibrv.*;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class RvSend {

    String service = null;
    String network = null;
    String daemon = null;

    String FIELD_NAME = "DATA";

    /**
     * @param args
     * @return
     */
    public RvSend(String... args) {
        // parse arguments for possible optional
        // parameters. These must precede the subject
        // and message strings
        args = addServerName(args);
        int i = get_InitParams(args);

        // we must have at least one subject and one message
        if (i > args.length - 2)
            usage();

        // open Tibrv in native implementation
        try {
            Tibrv.open(Tibrv.IMPL_NATIVE);
        } catch (TibrvException e) {
            System.err
                    .println("Failed to open Tibrv in native implementation:");
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

        // Create the message
        TibrvMsg msg = new TibrvMsg();

        // Set send subject into the message
        try {
            msg.setSendSubject(args[i++]);
        } catch (TibrvException e) {
            System.err.println("Failed to set send subject:");
            e.printStackTrace();
            System.exit(0);
        }

        try {
            // Send one message for each parameter
            while (i < args.length) {
                System.out.println("Publishing: subject="
                        + msg.getSendSubject() + " \"" + args[i] + "\"");
                msg.update(FIELD_NAME, args[i]);
                transport.send(msg);
                i++;
            }
        } catch (TibrvException e) {
            System.err.println("Error sending a message:");
            e.printStackTrace();
            System.exit(0);
        }

        // Close Tibrv, it will cleanup all underlying memory, destroy
        // transport and guarantee delivery.
        try {
            Tibrv.close();
        } catch (TibrvException e) {
            System.err.println("Exception dispatching default queue:");
            e.printStackTrace();
            System.exit(0);
        }

    }

    // print usage information and quit
    void usage() {
        System.err
                .println("Usage: java tibrvsend [-service service] [-network network]");
        System.err.println("            [-daemon daemon] <subject> <messages>");
        System.exit(-1);
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

    int get_InitParams(String[] args) {
        int i = 0;
        System.out.println(Arrays.toString(args));
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

    /**
     * 读取文本文件里的xml
     *
     * @return
     * @throws Exception
     */
    private static String readXML(String filePath) throws Exception {
        //
        StringBuilder sb = new StringBuilder();
        //
        File file = new File(filePath);
        //
        if (file.exists() && file.isFile()) {
            InputStreamReader rd = new InputStreamReader(new FileInputStream(
                    file), "UTF-8");
            BufferedReader bufferedReader = new BufferedReader(rd);
            String str = "";
            while ((str = bufferedReader.readLine()) != null) {
                sb.append(str);
            }
            rd.close();
        }
        //
        return sb.toString();
    }


    public static void main(String[] args) throws Exception {
        // 发送
        String service = "8210";
        String network = ";225.9.9.2";
        String daemon = "127.0.0.1:7500";
        String subject = "CHOT.G86.MES.TEST";
        String filePath="F:\\xml\\GetOicMainLotList.xml";
        String readXML = RvSend.readXML(filePath);
        RvSend rl = new RvSend(service, network, daemon, subject,readXML);
    }
}
