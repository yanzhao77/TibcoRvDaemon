package com.chot.utils;

import com.chot.service.serviceImpl.XmlForObjectServiceImpl;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

/**
 * @version 1.0
 * @Classname LoggerUtil
 * @Description TODO
 * @Date 2020/12/30 09:45
 * @Created by yan34177
 */
public class LoggerUtil {
    private Logger logger;

    private static class SingletonHolder {
        private static final LoggerUtil loggerUtil=new LoggerUtil();
    }


    public static final Logger getLogger() {
        return SingletonHolder.loggerUtil.logger;
    }

    private LoggerUtil() {
        loggerInit();
    }

    public void loggerInit() {
//        Path configrationPath = Paths.get("cfg", "log4j.properties");
//        if (!Files.exists(configrationPath) || !Files.isRegularFile(configrationPath)) {
//            return;
//
//        }
//        System.setProperty("log4j.defaultInitOverride", "1");
//        PropertyConfigurator.configure(configrationPath.toString());
        String resource = System.getProperty("user.dir") + "/resources/" + "log4j.properties";
        PropertyConfigurator.configure(resource);
        logger = Logger.getLogger(XmlForObjectServiceImpl.class.getName());
        logger.info("");
    }

}
