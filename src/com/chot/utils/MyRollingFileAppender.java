package com.chot.utils;


import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.RollingFileAppender;
import org.apache.log4j.helpers.CountingQuietWriter;
import org.apache.log4j.helpers.LogLog;
import org.apache.log4j.spi.LoggingEvent;


//继承log4j的RollingFileAppender类
public class MyRollingFileAppender extends RollingFileAppender {

    private long nextRollover = 0;
    private static Map<String, BeginFileData> fileMaps = new HashMap<String, BeginFileData>();
    private static final SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd");
    private static final SimpleDateFormat sdf1 = new SimpleDateFormat("yyyy-MM-dd");

    public void rollOver() {

        File target;
        File file;
        int maxBackupIndexLeng = String.valueOf(maxBackupIndex).length();
        if (qw != null) {
            long size = ((CountingQuietWriter) qw).getCount();
            LogLog.debug("rolling over count=" + size);
            nextRollover = size + maxFileSize;
        }
        //1
        LogLog.debug("maxBackupIndex=" + maxBackupIndex);
        String nowDateString = sdf.format(new Date());
        String newFileName = (fileName.indexOf(".") != -1 ? fileName.substring(0,
                fileName.lastIndexOf(".")) : fileName);

        boolean renameSucceeded = true;
        String nowDateStr = sdf1.format(new Date());
        if (maxBackupIndex > 0) {


//            file = new File(newFileName + '.' + nowDateString + '.'
//                    + getIndex(maxBackupIndex, maxBackupIndexLeng));

            file = new File(newFileName + '.' + nowDateStr + '.'
                    + getIndex(maxBackupIndex, maxBackupIndexLeng));

            if (file.exists()) {
                renameSucceeded = file.delete();
            }
            /**
             * 删除最后一个文件，文件名+1
             * */
            for (int i = maxBackupIndex - 1; (i >= 1 && renameSucceeded); i--) {
//                file = new File(newFileName + '.' + nowDateString + '.'
//                        + getIndex(i, maxBackupIndexLeng));
                file = new File(newFileName + '.' + nowDateStr + '.'
                        + getIndex(i, maxBackupIndexLeng));
                if (file.exists()) {
                    //行动文件名
//                    target = new File(newFileName + '.' + nowDateString + '.'
//                            + getIndex(i + 1, maxBackupIndexLeng));
                    target = new File(newFileName + '.' + nowDateStr + '.'
                            + getIndex(i + 1, maxBackupIndexLeng));
                    LogLog.debug("Renaming file " + file + " to " + target);
                    renameSucceeded = file.renameTo(target);
                }
            }

            if (renameSucceeded) {
                BeginFileData beginFileData = fileMaps.get(fileName);
                // 在每天一个日志目录的方式下，检测日期是否变更了，如果变更了就要把变更后的日志文件拷贝到变更后的日期目录下。
                String pattern = "yyyy/MM/dd";
                if (newFileName.indexOf(nowDateString) == -1
                        && beginFileData.getFileName().indexOf(pattern) != -1) {
                    newFileName = beginFileData.getFileName().replace(pattern,
                            nowDateString);
                    newFileName = (newFileName.indexOf(".") != -1 ? newFileName
                            .substring(0, newFileName.lastIndexOf(".")) : newFileName);
                }
//                target = new File(newFileName + '.' + nowDateString + '.'
//                        + getIndex(1, maxBackupIndexLeng));
                target = new File(newFileName + '.' + nowDateStr + '.'
                        + getIndex(1, maxBackupIndexLeng));
                this.closeFile();
                file = new File(fileName);
                LogLog.debug("Renaming file " + file + " to " + target);

                renameSucceeded = file.renameTo(target);
                if (!renameSucceeded) {
                    try {
                        this.setFile(fileName, true, bufferedIO, bufferSize);
                    } catch (IOException e) {
                        LogLog.error("setFile(" + fileName + ", true) call failed.", e);
                    }
                }
            }
        }
        if (renameSucceeded) {

            try {

                this.setFile(fileName, false, bufferedIO, bufferSize);
                nextRollover = 0;
            } catch (IOException e) {
                LogLog.error("setFile(" + fileName + ", false) call failed.", e);
            }
        }
        //删除指定日期前文件
        //清理指定日期前的日志
        String rootDir = (fileName.indexOf("/") != -1 ? fileName.substring(0,
                fileName.indexOf("/")) : fileName);
        long expireTime = 30 * 24 * 60 * 60 * 1000L; //表示日志保留天数
        cleanExpireFiles(rootDir, expireTime);
    }

    /**
     * 文件个数的长度补零，如果文件个数为10那么文件的个数长度就是2位，第一个文件就是01，02，03....
     *
     * @param i
     * @param maxBackupIndexLeng
     * @return
     */
    private String getIndex(int i, int maxBackupIndexLeng) {
        String index = String.valueOf(i);
        int len = index.length();
        for (int j = len; j < maxBackupIndexLeng; j++) {
            index = "0" + index;
        }
        return index + ".log";
    }

    /**
     * This method differentiates RollingFileAppender from its super class.
     *
     * @since 0.9.0
     */
    protected void subAppend(LoggingEvent event) {
        super.subAppend(event);
        if (fileName != null && qw != null) {

            String nowDate = sdf.format(new Date());
            // 检测日期是否已经变更了，如果变更了就要重创建日期目录
            if (!fileMaps.get(fileName).getDate().equals(nowDate)) {
                rollOver();
                return;
            }

            long size = ((CountingQuietWriter) qw).getCount();
            if (size >= maxFileSize && size >= nextRollover) {
                rollOver();
            }
        }
    }

    private void cleanExpireFiles(String savePath, long expireTime) {
        //日志根目录
        File fileRootDir = new File(savePath);
        if (!fileRootDir.exists()) {
            return;
        }
        //遍历根目录下的文件夹
        //如果文件是n天前的就删除
        //当前时间
        long currentTime = System.currentTimeMillis();
        File file = null;
        try {

            File[] files = fileRootDir.listFiles();
            if (files != null && files.length > 0) {
                for (int i = 0; i < files.length; i++) {
                    file = files[i];
                    long fileTime = file.lastModified();
                    if (file.isDirectory()) {
                        cleanExpireFiles(file.getAbsolutePath(), expireTime);
                        //文件夹内文件为空，则删除成功
                        file.delete();
                    } else {
                        //获取文件中的日期
                        String fileTimeStr = getRqStr(file.getName());
                        Date fileDate = null;
                        try {
                            if (fileTimeStr != null) {
                                fileDate = sdf1.parse(fileTimeStr);
                                fileTime = fileDate.getTime();
                            }
                        } catch (ParseException e) {
                            fileTime = currentTime;
//                            fileTime = file.lastModified();
                        }
                        //如果文件最后更新时间是N天前就删除
                        if (currentTime - fileTime > expireTime) {
                            file.delete();
                        }
                    }
                }
            }
        } catch (Exception e) {
            LogLog.error("删除过期日志失败!", e);
        }
    }

    @Override
    public synchronized void setFile(String fileName, boolean append,
                                     boolean bufferedIO, int bufferSize) throws IOException {

        String pattern = "yyyy/MM/dd";
        String nowDate = sdf.format(new Date());
        // 如果文件路径包含了“yyyy-MM-dd”就是每天一个日志目录的方式记录日志(第一次的时候)
        if (fileName.indexOf(pattern) != -1) {
            String beginFileName = fileName;
            fileName = fileName.replace(pattern, nowDate);
            fileMaps.put(fileName, new BeginFileData(beginFileName, nowDate));
        }
        BeginFileData beginFileData = fileMaps.get(fileName);
        // 检测日期是否已经变更了，如果变更了就要把原始的字符串给fileName变量，把变更后的日期做为开始日期
        if (!beginFileData.getDate().equals(nowDate)) {
            // 获取出第一次的文件名
            beginFileData.setDate(nowDate);
            fileName = beginFileData.getFileName().replace(pattern, nowDate);
            fileMaps.put(fileName, beginFileData);
        }

        // D:/data/test/yyyy-MM-dd/test.log 替换yyyy-MM-dd为当前日期。
        File file = new File(fileName);
        File parentFile = file.getParentFile();
        if (!parentFile.exists()) {
            parentFile.mkdirs();
        }

        super.setFile(fileName, append, this.bufferedIO, this.bufferSize);
    }

    /**
     * 获取文件中的日期
     *
     * @param mes
     * @return
     */
    public static String getRqStr(String mes) {
        String format = ".*([0-9]{4})-(0[1-9]|1[012])-(0[1-9]|[12][0-9]|3[01]).*";
        String yms = null;
        Pattern pattern = Pattern.compile(format);
        Matcher matcher = pattern.matcher(mes);
        if (matcher.matches()) {
            pattern = Pattern.compile(".*(\\d{4}-\\d{2}-\\d{2}).*");
            matcher = pattern.matcher(mes);
            if (matcher.matches()) {
                yms = matcher.group(1);
            }
            return yms;
        }
        return yms;

    }

    public static void main(String[] args) {
        String rootDir = "E:\\dell\\workspaces\\MyEclipse 10\\ljBank\\logger";
        long expireTime = 1 * 24 * 60 * 60 * 1000L;
        new MyRollingFileAppender().cleanExpireFiles(rootDir, expireTime);
    }

    private class BeginFileData {

        public BeginFileData(String fileName, String date) {
            super();
            this.fileName = fileName;
            this.date = date;
        }

        private String fileName;
        private String date;

        public String getFileName() {
            return fileName;
        }

        public void setFileName(String fileName) {
            this.fileName = fileName;
        }

        public String getDate() {
            return date;
        }

        public void setDate(String date) {
            this.date = date;
        }
    }
}