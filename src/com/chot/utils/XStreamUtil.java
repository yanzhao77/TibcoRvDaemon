package com.chot.utils;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.Dom4JDriver;
import com.thoughtworks.xstream.io.xml.Xpp3DomDriver;
import com.thoughtworks.xstream.io.xml.Xpp3Driver;
import com.thoughtworks.xstream.mapper.MapperWrapper;
import org.apache.log4j.Logger;

import java.io.*;
import java.text.DecimalFormat;

/**
 * @version 1.0
 * @Classname XStreamUtil
 * @Description TODO
 * @Date 2020/11/26 10:15
 * @Created by yan34177
 */
public class XStreamUtil {
    XStream xStream;
    static Logger logger;

    public XStreamUtil() throws Exception {
        //重写 wrapMapper 抑制XStream: UnknownFieldException - No such field问题
        //Dom4JDriver速度太慢，导致任务执行溢出队列，更换xpp3驱动
        xStream = new XStream(new Xpp3Driver()) {
            @Override
            protected MapperWrapper wrapMapper(MapperWrapper next) {
                return new MapperWrapper(next) {
                    @Override
                    public boolean shouldSerializeMember(Class definedIn, String fieldName) {
                        if (definedIn == Object.class) {
                            return this.realClass(fieldName) != null;
                        } else {
                            return super.shouldSerializeMember(definedIn, fieldName);
                        }
                    }
                };
            }
        };

    }

    /**
     * 将 xml内容转为 类对象
     *
     * @param xmlStr xml正文
     * @param cls    类
     * @param <T>
     * @return
     */
    @SuppressWarnings("unchecked")
    public <T> T toBean(String xmlStr, Class<T> cls) {
        xStream.setClassLoader(cls.getClassLoader());
        xStream.processAnnotations(cls);
        T obj = (T) xStream.fromXML(xmlStr);
        return obj;
    }

    /**
     * 将 obj转为 xml
     *
     * @param object 类对象
     * @return
     */
    public String toXML(Object object) throws Exception {
        logger.debug(object.toString());
        return xStream.toXML(object);
    }

    /**
     * 将 obj转为 xml文件
     *
     * @param xmlFileName xml文件名称
     * @param object      类对象
     * @return
     */
    public void toXMLFile(String xmlFileName, Object object) {
        try {
            PrintWriter printWriter = new PrintWriter(xmlFileName, "UTF-8");
            xStream.toXML(object, printWriter);
            logger.debug(object.toString());
        } catch (FileNotFoundException | UnsupportedEncodingException e) {
            // TODO Auto-generated catch block
            logger.error(e.getLocalizedMessage(), e.getCause());
        }
    }

    /**
     * 读取txt文件的内容
     *
     * @param filePath 文件地址
     * @return 返回文件内容
     */
    public String readFile(String filePath) {
        File file = new File(filePath);
        StringBuilder result = new StringBuilder();
        try {
            if (file.isFile() && file.exists()) { // 判断文件是否存在
                BufferedReader br = new BufferedReader(new FileReader(file));// 构造一个BufferedReader类来读取文件
                String s = null;
                while ((s = br.readLine()) != null) {// 使用readLine方法，一次读一行
                    result.append("\n").append(s);
                }
                br.close();
            }
        } catch (Exception e) {
            logger.error(e.getLocalizedMessage(), e.getCause());
        }
        return result.toString().trim();
    }

    /**
     * 写出txt文件的内容
     *
     * @param xmlvalue xml文件内容
     * @param filePath 文件地址
     * @return 返回文件内容
     */
    public static boolean writteFile(String xmlvalue, String filePath) {
        logger.debug(filePath);
        File file = new File(filePath);
        FileWriter filewriter = null;

        try {
            // 创建级联目录
            if (!file.getParentFile().exists()) {
                file.getParentFile().mkdirs();
            }
            // 创建文件夹
            if (!file.exists()) {
                file.createNewFile();
            }
            filewriter = new FileWriter(file);
            filewriter.write(xmlvalue);// 写出文件

        } catch (Exception e) {
            logger.error(e.getLocalizedMessage(), e.getCause());
        } finally {
            if (filewriter != null) {
                try {
                    filewriter.close();
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    logger.error(e.getLocalizedMessage(), e.getCause());
                }
            }
            return false;
        }
    }

    /**
     * byte转化为KB、MB、GB
     *
     * @param size
     * @return
     */
    public static String getNetFileSizeDescription(long size) {
        StringBuffer bytes = new StringBuffer();
        DecimalFormat format = new DecimalFormat("###.0");
        if (size >= 1024 * 1024 * 1024) {
            double i = (size / (1024.0 * 1024.0 * 1024.0));
            bytes.append(format.format(i)).append("GB");
        } else if (size >= 1024 * 1024) {
            double i = (size / (1024.0 * 1024.0));
            bytes.append(format.format(i)).append("MB");
        } else if (size >= 1024) {
            double i = (size / (1024.0));
            bytes.append(format.format(i)).append("KB");
        } else if (size < 1024) {
            if (size <= 0) {
                bytes.append("0B");
            } else {
                bytes.append((int) size).append("B");
            }
        }
        return bytes.toString();
    }
}