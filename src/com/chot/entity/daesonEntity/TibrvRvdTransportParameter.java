package com.chot.entity.daesonEntity;

import com.tibco.tibrv.TibrvException;
import com.tibco.tibrv.TibrvListener;
import com.tibco.tibrv.TibrvRvdTransport;

import java.util.*;

public class TibrvRvdTransportParameter {

    private String groupName = "default";//group名字 //daemon组，同一个group name的程序为一个群组，互相备份

    private int ftWeight = 50;//权重，由 1 到整數的最大值，數字越大優先權越大
    private int activeGoalNum = 2;//實務上要有幾支程式處於 ACTIVATE 狀態是可以設定的，這個數字稱為 active goal
    private double hbInterval = 1.5;//心跳时间间隔
    private double prepareInterval = 3;//准备时间间隔
    private double activateInterval = 4.8;//激活间隔
    boolean startInbox = false; //是否开启开启inbox
    private double lostInterval = 4.8;     // 活动成员的心跳间隔时间
    String service;
    String network;
    String daemon;
    String description;
    String[] subject;//频道集合
    String inbox;

    private TibrvRvdTransport tibrvRvdTransport;
    private Map<TibrvRvdTransport, List<TibrvListener>> tibrvListenerMap;
    private String messageName;//要检查的message

    public TibrvRvdTransportParameter(String service, String network, String daemon, String[] subject) {
        this.service = service;
        this.network = network;
        this.daemon = daemon;
        this.subject = subject;
    }

    public TibrvRvdTransportParameter(String messageName, String service, String network, String daemon, String[] subject) {
        this.messageName = messageName;
        this.service = service;
        this.network = network;
        this.daemon = daemon;
        this.subject = subject;
    }

    public TibrvRvdTransportParameter(String service, String network, String daemon, int ftWeight) {
        this.service = service;
        this.network = network;
        this.daemon = daemon;
        this.ftWeight = ftWeight;
    }

    /**
     * 设置名称和备注信息
     *
     * @param groupName
     * @param description
     */
    public void setFaultToleranceParameter(String groupName, String description) {
        this.groupName = groupName;
        this.setDescription(description);//添加说明
    }

    /**
     * 设置备用参数
     *
     * @param ftWeight
     * @param activeGoalNum
     * @param hbInterval
     * @param prepareInterval
     * @param activateInterval
     */
    public void setFaultToleranceParameter(int ftWeight, int activeGoalNum, double hbInterval, double prepareInterval, double activateInterval) {
        this.ftWeight = ftWeight;
        this.activeGoalNum = activeGoalNum;
        this.hbInterval = hbInterval;
        this.prepareInterval = prepareInterval;
        this.activateInterval = activateInterval;
    }

    /**
     * 获取TibrvRvdTransport
     *
     * @return
     * @throws TibrvException
     */
    public TibrvRvdTransport getTibrvRvdTransport() throws TibrvException {
        if (tibrvRvdTransport == null) {
            tibrvRvdTransport = new TibrvRvdTransport(service, network, daemon);
            tibrvRvdTransport.setDescription(description);
        }
        return tibrvRvdTransport;
    }

    public void setTibrvRvdTransport(TibrvRvdTransport tibrvRvdTransport) {
        this.tibrvRvdTransport = tibrvRvdTransport;
    }

    public int getFtWeight() {
        return ftWeight;
    }

    public void setFtWeight(int ftWeight) {
        this.ftWeight = ftWeight;
    }

    public int getActiveGoalNum() {
        return activeGoalNum;
    }

    public void setActiveGoalNum(int activeGoalNum) {
        this.activeGoalNum = activeGoalNum;
    }

    public double getHbInterval() {
        return hbInterval;
    }

    public void setHbInterval(double hbInterval) {
        this.hbInterval = hbInterval;
    }

    public double getPrepareInterval() {
        return prepareInterval;
    }

    public void setPrepareInterval(double prepareInterval) {
        this.prepareInterval = prepareInterval;
    }

    public double getActivateInterval() {
        return activateInterval;
    }

    public void setActivateInterval(double activateInterval) {
        this.activateInterval = activateInterval;
    }

    public String[] getSubject() {
        return subject;
    }

    /**
     * 获取唯一subjectName
     *
     * @return
     */
    public String getQuerySubjectName() {
        if (isStartInbox()) {
            return getSubject()[0];
        } else {
            return Arrays.toString(getSubject());
        }
    }

    public void setSubject(String[] subject) {
        this.subject = subject;
    }

    public String getGroupName() {
        return groupName;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }

    public boolean isStartInbox() {
        return startInbox;
    }

    public void setStartInbox(boolean startInbox) {
        this.startInbox = startInbox;
    }

    public String getMessageName() {
        return messageName;
    }

    public String getService() {
        return service;
    }

    public void setService(String service) {
        this.service = service;
    }

    public String getNetwork() {
        return network;
    }

    public void setNetwork(String network) {
        this.network = network;
    }

    public String getDaemon() {
        return daemon;
    }

    public void setDaemon(String daemon) {
        this.daemon = daemon;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setMessageName(String messageName) {
        this.messageName = messageName;
    }

    public double getLostInterval() {
        return lostInterval;
    }

    public void setLostInterval(double lostInterval) {
        this.lostInterval = lostInterval;
    }

    public String getQueryInbox() throws TibrvException {
        if (!isStartInbox()) {
            return null;
        }
        return null != inbox ? inbox : getTibrvRvdTransport().createInbox();
    }

    public Map<TibrvRvdTransport, List<TibrvListener>> getTibrvListenerMap() {
        if (tibrvListenerMap == null) {
            tibrvListenerMap = new HashMap<>();
        }
        return tibrvListenerMap;
    }

    public void setTibrvListenerMap(TibrvRvdTransport transport, TibrvListener listener) {
        if (getTibrvListenerMap().get(transport) == null) {
            getTibrvListenerMap().put(transport, new ArrayList<>());
        }
        getTibrvListenerMap().get(transport).add(listener);

    }
}