package com.chot.rvLister;

import com.chot.messageCheck.MessageRead;
import com.tibco.tibrv.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Rvlistener implements TibrvMsgCallback {

    String service = null;
    String network = null;
    String daemon = null;

    MessageRead messageRead;

    public Rvlistener() {

    }

    public void init(String... args) {
        // parse arguments for possible optional
        // parameters. These must precede the subject
        // and message strings

        args = addServerName(args);
        int i = get_InitParams(args);

        // we must have at least one subject
        if (i >= args.length)
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

        // Create listeners for specified subjects
        while (i < args.length) {
            // create listener using default queue
            try {
                new TibrvListener(Tibrv.defaultQueue(), this, transport,
                        args[i], null);
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
//        System.out.println((new Date()).toString() +
//                ": subject=" + msg.getSendSubject() +
//                ", reply=" + msg.getReplySubject() +
//                ", message=" + msg.toString()
//        );
//        List<String> messagename = match(msg.toString(), "MESSAGENAME");
        if (msg.toString().contains("GetOicMainLotList")) {
            messageRead.readMessage(msg);
        }


        System.out.flush();
    }

    // print usage information and quit
    void usage() {
        System.err
                .println("Usage: java tibrvlisten [-service service] [-network network]");
        System.err.println("            [-daemon daemon] <subject-list>");
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

    /**
     * 添加参数
     *
     * @param args
     * @return
     */
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

    public MessageRead getMessageRead() {
        return messageRead;
    }

    public void setMessageRead(MessageRead messageRead) {
        this.messageRead = messageRead;
    }

    public static void main(String[] args) {
        // 监听

        String service = "8210";
        String network = ";225.9.9.2";
        String daemon = "10.50.10.72:7500";
//        String subject = "CHOT.G86.MES.TEST.PEMsvr";

//        String service = "8200";
//        String network = ";225.16.16.2";
//        String daemon = "10.50.10.72:7500";
        // 监听多个server
        String subject = "CHOT.G86.ACFMES.PROD.PEMsvr";
        String subjectOC = "CHOT.G86.OCMES.PROD.PEMsvr";
        String subjectCommon = "CHOT.G86.MES.PROD.PEMsvr";
        Rvlistener rl = new Rvlistener();
        rl.init(service, network, daemon, subject, subjectOC, subjectCommon);
    }

}
