package com.chot.service.serviceImpl;

import com.chot.messageCheck.MessageReadCallback;
import com.chot.rvLister.RvListener;
import com.chot.utils.CustomThreadPoolExecutor;
import com.tibco.tibrv.TibrvListener;
import com.tibco.tibrv.TibrvMsg;

import org.apache.log4j.Logger;
import org.dom4j.Attribute;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.dom4j.tree.DefaultElement;

import java.io.*;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class XmlReadFactory {
    MessageReadCallback messageRead;// 设置回调验证消息
    Class MessageClass; // xml文件映射类
    CustomThreadPoolExecutor customThreadPoolExecutor;//线程池
    XMLService xmlService;
    RvListener rvlistener;
    Map<String, String> subjectNameForCheckMessageNameMap;
    Logger logger;


    public XmlReadFactory(Logger logger) {
        this.logger = logger;
        rvlistener = new RvListener();
        customThreadPoolExecutor = new CustomThreadPoolExecutor();
        customThreadPoolExecutor.init();
        messageRead = new MessageReadCallback() {
            @Override
            public void onMsg(TibrvListener tibrvListener, TibrvMsg tibrvMsg) {
                //如果有消息传入，则分配一个线程执行消息处理
                customThreadPoolExecutor.execute(new Runnable() {
                    @Override
                    public void run() {
                        String message = checkMessage(tibrvMsg.toString());// message全文
                        String checkMessageName = getCheckMessageName(tibrvListener.getSubject());
                        if (checkMessageName == null) {
                            logger.debug(message);
                            return;
                        }
                        String readMessageCheck = DocumentReadMessageCheck(message, tibrvMsg, checkMessageName);// 取出xml正文
                        if (readMessageCheck != null) {
                            xmlService.toJavaBan(readMessageCheck, MessageClass, tibrvMsg);
                        }
                        // 如果xStream无法识别，就使用map解析
                        // Map<String, Object> objectMap =
                        // ParseXML.parserXml(message);
                        // messageValue = objectMap;
                    }
                });
            }
        };
    }

    /**
     * 根据messageName 验证
     *
     * @param message
     * @param messageName
     * @param msg
     */
    public String DocumentReadMessageCheck(String message, TibrvMsg msg,
                                           String messageName) {
        // 解析books.xml文件
        // 创建SAXReader的对象reader
        SAXReader reader = new SAXReader();
        InputStream stringStream = getStringStream(message);

        try {
            // 通过reader对象的read方法加载books.xml文件,获取docuemnt对象。
            Document document = reader.read(stringStream);
            // 通过document对象获取根节点bookstore
            Element bookStore = document.getRootElement();
            // 通过element对象的elementIterator方法获取迭代器
            Iterator it = bookStore.elementIterator();
            // 遍历迭代器，获取根节点中的信息（书籍）
            while (it.hasNext()) {
                DefaultElement book = (DefaultElement) it.next();
                // 获取book的属性名以及 属性值
                List<Attribute> bookAttrs = book.attributes();
                for (Attribute attr : bookAttrs) {
                    System.out.println("属性名：" + attr.getName() + "--属性值："
                            + attr.getValue());
                }
                Iterator itt = book.elementIterator();
                while (itt.hasNext()) {
                    Element element = (Element) itt.next();
                    if (element.getStringValue().equals(messageName)) {
                        // 临时写出xml文件
                        // xStreamUtil.writteFile(message, "F:\\xml\\"
                        // + messageName + "\\" + messageName
                        // + new Date().getTime() + ".xml");
                        if (element.getName().equals("MESSAGENAME")) {// 打印消息名称
                            logger.debug("subject=" + msg.getSendSubject()
                                    + ", reply=" + msg.getReplySubject()
                                    + ", messageName=" + element.getStringValue());
                        }
                        // 如果消息名称相同，就返回
                        return message;
                    }

                }
            }
        } catch (DocumentException e) {
            // TODO Auto-generated catch block
            logger.error(e.getLocalizedMessage(), e.getCause());
        }
        return null;
    }

    /**
     * 字符串转输入流
     *
     * @param sInputString
     * @return
     */
    public InputStream getStringStream(String sInputString) {
        if (sInputString != null && !sInputString.trim().equals("")) {
            try {
                ByteArrayInputStream tInputStringStream = new ByteArrayInputStream(
                        sInputString.getBytes());
                return tInputStringStream;
            } catch (Exception ex) {
                logger.error(ex.getLocalizedMessage(), ex.getCause());
            }
        }
        return null;
    }

    /**
     * 取出字符
     *
     * @param message
     * @return
     */
    public String checkMessage(String message) {
        String string = splitStr(message, "\\{([^}]*)\\}");// 取出大括号中的内容
        if (string.contains("xmlData=")) {
            String substring = string.replaceAll("xmlData=\"", "");
            // String substring = string.substring(0,string.indexOf("xmlData=")
            // + 1);
            return substring.substring(1, substring.lastIndexOf("\""));
        } else {
            return string;
        }
    }

    /**
     * 正则表达式取出字符
     *
     * @param str
     * @param pattern
     * @return
     */
    public String splitStr(String str, String pattern) {
        Pattern p = Pattern.compile(pattern);
        Matcher matcher = p.matcher(str);
        if (matcher.find() && matcher.groupCount() >= 1) {
            return matcher.group(1);
        }
        return null;
    }


    /**
     * 获取监听的消息名称，获取消息的实体类
     *
     * @param checkMessageName
     * @throws ClassNotFoundException
     */
    public <T> void rvlistenerInit(String checkMessageName, String... subjectNameArr)
            throws ClassNotFoundException {
        if (checkMessageName == null) return;
        for (String subjectName : subjectNameArr) {
            setCheckMessageName(subjectName, checkMessageName);
        }
        this.MessageClass = Class
                .forName("com.chot.entity.messageEntity." + checkMessageName);
    }

    /**
     * 启动监听
     */
    public void start() {
        xmlService = new XMLService();
        rvlistener.start();
        logger.debug("start rvlistener");
    }

    /**
     * 初始化监听，并启动监听
     *
     * @param checkMessageName 监听的消息名称
     * @param service
     * @param network
     * @param daemon
     * @param isStartInbox
     * @param subjectNames     监听的频道参数
     */
    public void init(String checkMessageName, String service, String network, String daemon, boolean isStartInbox, String... subjectNames) {
        try {
            rvlistenerInit(checkMessageName, subjectNames);
        } catch (ClassNotFoundException e) {
            // TODO Auto-generated catch block
            logger.error(e.getLocalizedMessage(), e.getCause());
        }
        rvlistener.setMessageRead(getMessageRead());
        rvlistener.setTransportParameter(null, checkMessageName, service, network, daemon, isStartInbox, subjectNames);


    }

    /**
     * 初始化机组监听，并启动监听
     *
     * @param groupName        主备机组名称
     * @param checkMessageName 监听的消息名称
     * @param serviceArr
     * @param isStartInbox
     * @param subjectNames     监听的频道参数
     */
    public void initGroups(String groupName, String checkMessageName, String[][] serviceArr, boolean isStartInbox, String... subjectNames) {
        try {
            rvlistenerInit(checkMessageName, subjectNames);
        } catch (ClassNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        rvlistener.setMessageRead(getMessageRead());
        Map<String, List<String[]>> stringListMap = new HashMap<>();
        List<String[]> serviceList = new ArrayList<>();
        for (String[] strings : serviceArr) {
            serviceList.add(strings);
        }
        stringListMap.put(groupName, serviceList);
        rvlistener.setTransportParameterGroup(stringListMap, checkMessageName, isStartInbox, subjectNames);
        xmlService = new XMLService();
    }

    /**
     * 数组合并
     *
     * @param first
     * @param second
     * @param <T>
     * @return
     */
    public static <T> T[] concat(T[] first, T[] second) {
        T[] result = Arrays.copyOf(first, first.length + second.length);
        System.arraycopy(second, 0, result, first.length, second.length);
        return result;
    }

    public Class getMessageClass() {
        return MessageClass;
    }

    public MessageReadCallback getMessageRead() {
        return messageRead;
    }

    public void setMessageRead(MessageReadCallback messageRead) {
        this.messageRead = messageRead;
    }

    public Map<String, String> getSubjectNameForCheckMessageNameMap() {
        if (subjectNameForCheckMessageNameMap == null) {
            subjectNameForCheckMessageNameMap = new HashMap<>();
        }
        return subjectNameForCheckMessageNameMap;
    }

    public void setSubjectNameForCheckMessageNameMap(Map<String, String> messageNameMap) {
        this.subjectNameForCheckMessageNameMap = messageNameMap;
    }

    public String getCheckMessageName(String subjectName) {
        return subjectNameForCheckMessageNameMap.get(subjectName);
    }

    public void setCheckMessageName(String subjectName, String checkMessageName) {
        getSubjectNameForCheckMessageNameMap().put(subjectName, checkMessageName);
    }

}
