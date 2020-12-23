package com.chot.test.demo;


import com.chot.messageCheck.MessageReadCallback;
import com.chot.rvDaesonGroup.TibrvRvdTransportParameter;
import com.tibco.tibrv.*;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Rvlistener implements TibrvMsgCallback {

    String service = null;
    String network = null;
    String daemon = null;

    boolean startInbox = false; //开启inbox
    static String query_subject;   // To find the server 服务器
    static String response_subject;     //inbox 名称

    MessageReadCallback messageRead;
    Map<String, List<TibrvRvdTransportParameter>> tibrvRvdTransportGroup;//备用机组

    public Rvlistener() {
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
     * 主备检查
     *
     * @throws TibrvException
     */
    public TibrvRvdTransportParameter chackFailover() throws TibrvException {
        if (getTibrvRvdTransportGroup().size() > 0) {
            for (String key : getTibrvRvdTransportGroup().keySet()) {
                List<TibrvRvdTransportParameter> transportParameterList = getTibrvRvdTransportGroup().get(key);
                for (TibrvRvdTransportParameter tibrvRvdTranspor : transportParameterList) {
                    if (!tibrvRvdTranspor.getService().equals(service) & !tibrvRvdTranspor.getNetwork().equals(network) &
                            !tibrvRvdTranspor.getDaemon().equals(daemon)) {
                        return tibrvRvdTranspor;
                    }
                }
            }
        }
        return null;
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


        // Create RVD transport
        TibrvTransport transport = null;
        try {
            transport = new TibrvRvdTransport(service, network, daemon);
        } catch (TibrvException e) {
            System.err.println("Failed to create TibrvRvdTransport:");
            e.printStackTrace();
            System.exit(0);
        }

        // Create a response queue
        TibrvQueue tibrvQueue = null;
        try {
            tibrvQueue = startInbox ? new TibrvQueue() : Tibrv.defaultQueue();
        } catch (TibrvException e) {
            System.err.println("Failed to create TibrvQueue:");
            e.printStackTrace();
            System.exit(0);
        }

        if (startInbox) {//如果开启点对点通信，就创建
            // Create an inbox subject for communication with the server and
            // create a listener for this response subject.
            //创建与服务器通信的收件箱主题，并为此响应主题创建侦听器。
            try {
                query_subject = args[i];
                response_subject = transport.createInbox();
                new TibrvListener(tibrvQueue,//创建inbox监听
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

            TibrvMsg reply_msg = null;
            try {
                reply_msg = transport.sendRequest(query_msg, 3);
            } catch (TibrvException e) {
                System.err.println("Failed to detect server:");
                e.printStackTrace();
                System.exit(0);
            }

            // If timeout, reply message is null and query failed.
            if (reply_msg == null) {

                try {
                    TibrvRvdTransportParameter tibrvRvdTransportParameter = chackFailover();
//                    rl.init(tibrvRvdTransportParameter.getService(), network, daemon, subject);
                } catch (TibrvException e) {
                    e.printStackTrace();
                }

//                System.err.println("Failed to detect server.");
//                System.exit(0);
            }
            // Report finding a server.
            TibrvMsg server_msg = new TibrvMsg();
            String server_subject = reply_msg.getReplySubject();

            // Create a dispatcher with 5 second timeout to process server replies
            TibrvDispatcher dispatcher = new TibrvDispatcher("Dispatcher", tibrvQueue, 5.0);

            try {
                server_msg.setSendSubject(server_subject);
                server_msg.setReplySubject(response_subject);
            } catch (TibrvException e) {
                System.err.println("Failed to set subjects, fields for test message:");
                e.printStackTrace();
                System.exit(0);
            }
        } else {
            // Create listeners for specified subjects
            while (i < args.length) {
                // create listener using default queue
                try {
                    new TibrvListener(tibrvQueue, this, transport,
                            args[i], null);
                    System.err.println("Listening on: " + args[i]);
                } catch (TibrvException e) {
                    System.err.println("Failed to create listener:");
                    e.printStackTrace();
                    System.exit(0);
                }
                i++;
            }
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
        System.out.println((new Date()).toString() +
                ": subject=" + msg.getSendSubject() +
                ", reply=" + msg.getReplySubject() +
                ", message=" + msg.toString()
        );
//        List<String> messagename = match(msg.toString(), "MESSAGENAME");
        if (messageRead != null)
            messageRead.readMessage(msg);

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

    public MessageReadCallback getMessageRead() {
        return messageRead;
    }

    public void setMessageRead(MessageReadCallback messageRead) {
        this.messageRead = messageRead;
    }

    public boolean isStartInbox() {
        return startInbox;
    }

    public void setStartInbox(boolean startInbox) {
        this.startInbox = startInbox;
    }

    public Map<String, List<TibrvRvdTransportParameter>> getTibrvRvdTransportGroup() throws TibrvException {
        return tibrvRvdTransportGroup == null ? tibrvRvdTransportGroup = new HashMap<>() : tibrvRvdTransportGroup;
    }

    /**
     * 添加备用机
     *
     * @param groupName
     * @param tibrvRvdTransportGroup
     * @throws TibrvException
     */
    public void setTibrvRvdTransportGroup(String groupName, TibrvRvdTransportParameter tibrvRvdTransportGroup) throws TibrvException {
        List<TibrvRvdTransportParameter> transportParameterList = new ArrayList();
        if (getTibrvRvdTransportGroup().get(groupName) == null) {
            getTibrvRvdTransportGroup().put(groupName, transportParameterList);
        } else {
            transportParameterList = getTibrvRvdTransportGroup().get(groupName);
        }
        transportParameterList.add(tibrvRvdTransportGroup);
    }

    public static void main(String[] args) throws TibrvException {
        // 监听
        String service = "8210";
        String network = ";225.9.9.2";
        String daemon = "127.0.0.1:7500";
        String subject = "CHOT.G86.MES.TEST.PEMsvr2";
        Rvlistener rl = new Rvlistener();
        rl.setStartInbox(true);
        rl.setTibrvRvdTransportGroup("default", new TibrvRvdTransportParameter(service, network, daemon, subject));
        rl.setTibrvRvdTransportGroup("default", new TibrvRvdTransportParameter(
                service, network, daemon, subject));
        rl.init(service, network, daemon, subject);
    }


}
