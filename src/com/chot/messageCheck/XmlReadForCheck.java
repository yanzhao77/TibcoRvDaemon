package com.chot.messageCheck;

import com.chot.entity.CheckecipeParameterRequest;
import com.chot.entity.GetOicMainLotList;
import com.chot.rvLister.Rvlistener;
import com.chot.utils.ParseXML;
import com.chot.utils.XStreamUtil;
import com.tibco.tibrv.TibrvMsg;

import org.dom4j.Attribute;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.dom4j.tree.DefaultElement;

import javax.xml.bind.JAXBContext;

import java.io.*;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class XmlReadForCheck {
    ParseXML parseXML;
    XStreamUtil xStreamUtil;
    MessageRead messageRead;// 设置回调验证消息
    String checkMessageName;// 要拦截的消息名称
    Object messageValue;// 抓取的消息obj
    Class MessageClass; // xml文件映射类

    public XmlReadForCheck() {

        parseXML = new ParseXML();
        xStreamUtil = new XStreamUtil();
        messageRead = new MessageRead() {
            @Override
            public void readMessage(TibrvMsg msg) {
                String message = checkMessage(msg.toString());// message全文
                String readMessageCheck = DocumentReadMessageCheck(message,
                        msg, checkMessageName);// 取出xml正文

                if (readMessageCheck != null) {
                    messageValue = xStreamUtil.toBean(readMessageCheck,
                            MessageClass);
                    // 如果xStream无法识别，就使用map解析
                    // Map<String, Object> objectMap =
                    // parseXML.parserXml(message);
                    // messageValue = objectMap;

                    if (messageValue != null) {
                        println(messageValue);// 打印
                    }
                }
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
                            System.out.println("subject="
                                    + msg.getSendSubject() + ", reply="
                                    + msg.getReplySubject()
                                    + ", messageName="
                                    + element.getStringValue());
                        }
                        // 如果消息名称相同，就返回
                        return message;
                    }

                }
            }
        } catch (DocumentException e) {
            // TODO Auto-generated catch block
            System.err.println(e.getMessage());
        }
        return null;
    }

    /**
     * 字符串转输入流
     *
     * @param sInputString
     * @return
     */
    public static InputStream getStringStream(String sInputString) {
        if (sInputString != null && !sInputString.trim().equals("")) {
            try {
                ByteArrayInputStream tInputStringStream = new ByteArrayInputStream(
                        sInputString.getBytes());
                return tInputStringStream;
            } catch (Exception ex) {
                ex.printStackTrace();
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
     * 打印消息
     *
     * @param message
     */
    public void println(Object message) {
        if (message instanceof GetOicMainLotList) {
            GetOicMainLotList messageEntity = (GetOicMainLotList) message;

            System.out.println(messageEntity.getBody().getFactoryName());
            System.out.println(messageEntity.getBody().getMachineName());
            System.out.println(messageEntity.getBody().getEventUser());
            System.out.println(messageEntity.getBody().getSoftwareVersion());
            System.out.println(messageEntity.getBody()
                    .getTransactionStartTime());
            System.out.println();
        } else if (message instanceof CheckecipeParameterRequest) {
            CheckecipeParameterRequest checkMessage = new CheckecipeParameterRequest();
            System.out.println(checkMessage.getHeader().getMessageName());
            System.out.println(checkMessage.getBody().getLineName());
        }
    }

    /**
     * 获取监听的消息名称，获取消息的实体类
     *
     * @param checkMessageName
     * @throws ClassNotFoundException
     */
    public <T> void rvlistenerInit(String checkMessageName)
            throws ClassNotFoundException {
        setCheckMessageName(checkMessageName);
        this.MessageClass = Class
                .forName("com.chot.entity." + checkMessageName);
    }


    /**
     * 初始化监听，并启动监听
     *
     * @param checkMessageName 监听的消息名称
     * @param service
     * @param network
     * @param daemon
     * @param subjectNames     监听的频道参数
     */
    public void init(String checkMessageName, String service, String network, String daemon, String... subjectNames) {
        try {
            rvlistenerInit(checkMessageName);
        } catch (ClassNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        Rvlistener rl = new Rvlistener();
        rl.setMessageRead(getMessageRead());
        String[] services = new String[3];
        services[0] = service;
        services[1] = network;
        services[2] = daemon;
        String[] args = concat(services, subjectNames);
        rl.init(args);
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

    public MessageRead getMessageRead() {
        return messageRead;
    }

    public void setMessageRead(MessageRead messageRead) {
        this.messageRead = messageRead;
    }

    public String getCheckMessageName() {
        return checkMessageName;
    }

    public void setCheckMessageName(String checkMessageName) {
        this.checkMessageName = checkMessageName;
    }

}
