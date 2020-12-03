package com.chot.utils;

import org.apache.commons.beanutils.BeanUtils;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

import java.io.*;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @ClassName: ParseXML
 * @Description: 递归解析xml
 * @author:yz <p>
 * <p>
 */
public class ParseXML {

    /**
     * 读取xml中的数据
     *
     * @param xml
     * @return
     * @throws Exception
     */
    public Map<String, Object> parserXml(String xml) {
        StringReader reader = new StringReader(xml);
        // 创建一个新的SAXBuilder
        SAXReader sb = new SAXReader();
        //
        Map<String, Object> result = new HashMap<String, Object>();
        // 通过输入源构造一个Document
        try {
            Document doc = sb.read(reader);
            // 取的根元素
            Element root = doc.getRootElement();
            List children = root.elements();
            // 解析xml
            result = toMap(children, null);
        } catch (DocumentException e) {// 如果是空的，就是这个message不是个xml
            return null;
        } catch (Exception e) {
            System.err.print(e.getMessage());
            throw e;
        } finally {
            if (reader != null) {
                reader.close();
            }
        }
        return result;
    }

    /**
     * 递归解析xml，实现N层解析
     *
     * @param elements
     * @param list
     * @return
     */
    private Map<String, Object> toMap(List<Element> elements,
                                      List<Map<String, Object>> list) {
        //
        Element el = null;
        String name = "";
        //
        Map<String, Object> map = new HashMap<String, Object>();
        //
        for (int i = 0; i < elements.size(); i++) {
            el = (Element) elements.get(i);
            name = el.getName();
            // 如果是定义成数组
            if (el.hasMixedContent()) {// 判断是否有子节点
                // 继续递归循环
                List<Map<String, Object>> sublist = new ArrayList<Map<String, Object>>();
                //
                Map<String, Object> subMap = this.toMap(el.elements(), sublist);
                // 根据key获取是否已经存在
                Object object = map.get(name);
                // 如果存在,合并
                if (object != null) {
                    List<Map<String, Object>> olist = (List<Map<String, Object>>) object;
                    olist.add(subMap);//
                    map.put(name, olist);
                } else {// 否则直接存入map
                    map.put(name, sublist);
                }
            } else {// 单个值存入map
                map.put(name, el.getTextTrim());
            }
        }
        // 存入list中
        if (list != null)
            list.add(map);
        // 返回结果集合
        return map;
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

    public Map<String, Object> fromJavaBean(Object bean) {
        if (null == bean)
            return null;

        try {
            Map<String, Object> map = BeanUtils.describe(bean);
            return map;
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        }
        return null;
    }

    public <T> T toJavaBean(Class<?> clazz, Map map) {
        try {
            T newBeanInstance = (T) clazz.newInstance();

            BeanUtils.populate(newBeanInstance, map);
            return newBeanInstance;
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 验证这个xml是否是需要的数据
     *
     * @param result
     * @param messageName
     * @return
     */
    public boolean checkMapForMessageName(Map<String, Object> result,
                                          String messageName) {
        List<Map<String, String>> headerMapList = (List<Map<String, String>>) result
                .get("HEADER");
        for (Map<String, String> headerMap : headerMapList) {
            return headerMap.get("messageName".toUpperCase()).equals(
                    messageName);
        }
        return false;
    }

}
