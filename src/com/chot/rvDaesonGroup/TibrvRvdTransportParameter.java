package com.chot.rvDaesonGroup;

import com.tibco.tibrv.TibrvException;
import com.tibco.tibrv.TibrvRvdTransport;

public class TibrvRvdTransportParameter extends TibrvRvdTransport {

    private String groupName;//group名字 //daemon组，同一个group name的程序为一个群组，互相备份

    private int ftWeight = 50;//权重，由 1 到整數的最大值，數字越大優先權越大
    private int activeGoalNum = 1;//實務上要有幾支程式處於 ACTIVATE 狀態是可以設定的，這個數字稱為 active goal
    private double hbInterval = 1.5;//心跳时间间隔
    private double prepareInterval = 3;//准备时间间隔
    private double activateInterval = 4.8;//激活间隔

    private String[] subject;//频道集合

    public TibrvRvdTransportParameter(String service, String network, String daemon, String... subject) throws TibrvException {
        super(service, network, daemon);
        this.subject = subject;
    }

    /**
     * 设置名称和备注信息
     *
     * @param groupName
     * @param description
     */
    public void setFaultToleranceParameter(String groupName, String description) throws TibrvException {
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

    public void setSubject(String[] subject) {
        this.subject = subject;
    }

    public String getGroupName() {
        return groupName;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }
}