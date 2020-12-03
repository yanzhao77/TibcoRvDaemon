package com.chot.utils;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.Dom4JDriver;

import java.io.*;

/**
 * @version 1.0
 * @Classname XStreamUtil
 * @Description TODO
 * @Date 2020/11/26 10:15
 * @Created by yan34177
 */
public class XStreamUtil {
    XStream xStream;

    public XStreamUtil() {
        xStream = new XStream(new Dom4JDriver());

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
        } catch (FileNotFoundException | UnsupportedEncodingException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
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
            e.printStackTrace();
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
        System.err.println(filePath);
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
            e.printStackTrace();
        } finally {
            if (filewriter != null) {
                try {
                    filewriter.close();
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
            return false;
        }
    }

}