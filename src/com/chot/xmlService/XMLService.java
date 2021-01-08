package com.chot.xmlService;

import com.chot.entity.messageEntity.CheckRecipeParameterRequest;
import com.chot.entity.messageEntity.GetOicMainLotList;
import com.chot.utils.LoggerUtil;
import com.chot.utils.ParseXML;
import com.chot.utils.XStreamUtil;
import com.tibco.tibrv.TibrvListener;
import com.tibco.tibrv.TibrvMsg;
import org.apache.log4j.Logger;
import org.dom4j.Attribute;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.dom4j.tree.DefaultElement;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @version 1.0
 * @Classname XMLreadService
 * @Description TODO
 * @Date 2020/12/18 11:03
 * @Created by yan34177
 */
public class XMLService {
    Map<String, String> subjectNameForCheckMessageNameMap;//监听的频道和监听的messageName
    Map<String, XStreamUtil> messageXStreamMap;//类加载器
    Logger logger;

    public XMLService() {
        logger = LoggerUtil.getLogger();
    }


    /**
     * 打印消息
     *
     * @param messageValue
     * @param message
     */
    public void println(Object messageValue, TibrvMsg message) {
        Thread thread = Thread.currentThread();
        logger.info("线程ID：" + thread.getId() + "\t线程名称：" + thread.getName());
        System.out.println(message.getSendSubject() + "\t" + messageValue.getClass().getSimpleName());
        if (messageValue instanceof GetOicMainLotList) {
            GetOicMainLotList messageEntity = (GetOicMainLotList) messageValue;

            System.out.println(messageEntity.getBody().getFactoryName());
            System.out.println(messageEntity.getBody().getMachineName());
            System.out.println(messageEntity.getBody().getEventUser());
            System.out.println(messageEntity.getBody().getSoftwareVersion());
            System.out.println(messageEntity.getBody()
                    .getTransactionStartTime());
            System.out.println();
        } else if (messageValue instanceof CheckRecipeParameterRequest) {
            CheckRecipeParameterRequest checkMessage = (CheckRecipeParameterRequest) messageValue;
            System.out.println(checkMessage.getHeader().getMessageName());
            System.out.println(checkMessage.getBody().getLineName());
        }
    }

    public void xmlCheckForMessage(String checkMessage, TibrvListener tibrvListener, TibrvMsg tibrvMsg) {
        String message = checkMessage(checkMessage);// message全文
        String checkMessageName = getCheckMessageName(tibrvListener.getSubject());
        if (checkMessageName == null) {
//            logger.debug(message);
            return;
        }
        String readMessageCheck = documentReadMessageCheck(message, tibrvMsg, checkMessageName);// 取出xml正文
        if (readMessageCheck != null) {
            toJavaBan(readMessageCheck, checkMessageName, tibrvMsg);
        }
        // 如果xStream无法识别，就使用map解析
//        Map<String, Object> objectMap = ParseXML.parserXml(message);
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
     * 根据messageName 验证
     *
     * @param message
     * @param messageName
     * @param msg
     */
    public String documentReadMessageCheck(String message, TibrvMsg msg,
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
     * 获取监听的消息名称，获取消息的实体类
     *
     * @param checkMessageName
     * @throws ClassNotFoundException
     */
    public void rvlistenerInit(String checkMessageName, String... subjectNameArr) {
        if (checkMessageName == null) return;
        for (String subjectName : subjectNameArr) {
            try {
                setCheckMessageName(subjectName, checkMessageName);
            } catch (ClassNotFoundException e) {
                logger.error(e.getLocalizedMessage());
            } catch (Exception e) {
                logger.error(e.getLocalizedMessage());
            }
        }
    }

    public void toJavaBan(String readMessage, String checkMessageName, TibrvMsg msg) {
        Object messageValue = null;// 抓取的消息obj
        try {
            Class cls = getClassForCheckMessageName(checkMessageName);
            messageValue = getMessageXStreamMap().get(checkMessageName).toBean(readMessage, cls);
        } catch (Exception e) {
            logger.error(e.getLocalizedMessage());
            logger.error(readMessage);
        }
        if (messageValue != null) {
            println(messageValue, msg);// 打印
        }
    }

    /**
     * 根据消息名称获取类对象
     *
     * @param checkMessageName
     * @param <T>
     * @return
     * @throws ClassNotFoundException
     */
    public <T> T getClassForCheckMessageName(String checkMessageName) throws ClassNotFoundException {
        return (T) Class.forName("com.chot.entity.messageEntity." + checkMessageName);
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


    public Map<String, String> getSubjectNameForCheckMessageNameMap() {
        if (subjectNameForCheckMessageNameMap == null) {
            subjectNameForCheckMessageNameMap = new HashMap<>();
        }
        return subjectNameForCheckMessageNameMap;
    }

    public void setSubjectNameForCheckMessageNameMap(Map<String, String> messageNameMap) {
        this.subjectNameForCheckMessageNameMap = messageNameMap;
    }

    public Map<String, XStreamUtil> getMessageXStreamMap() {
        if (messageXStreamMap == null) {
            messageXStreamMap = new HashMap<>();
        }
        return messageXStreamMap;
    }

    public void setMessageXStreamMap(Map<String, XStreamUtil> messageClassMap) {
        this.messageXStreamMap = messageClassMap;
    }

    public String getCheckMessageName(String subjectName) {
        return getSubjectNameForCheckMessageNameMap().get(subjectName);
    }

    public void setCheckMessageName(String subjectName, String checkMessageName) throws Exception {
        getSubjectNameForCheckMessageNameMap().put(subjectName, checkMessageName);
        getMessageXStreamMap().put(checkMessageName, new XStreamUtil());
    }

}
