package com.chot.entity;


import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamAsAttribute;

import java.util.List;


@XStreamAlias("MESSAGE")
public class GetOicMainLotList {
    @XStreamAlias("HEADER")//别名，在xml中的名字
    @XStreamAsAttribute//如果是属性值，就加标识
    Header header;

    @XStreamAlias("BODY")
    @XStreamAsAttribute
    Body body;

    @XStreamAlias("RESULT")
    @XStreamAsAttribute
    Result result;

    @XStreamAlias("RETURN")
    @XStreamAsAttribute
    Header.ReturnMessage Return;

    public GetOicMainLotList() {
    }

    public Header getHeader() {
        return header;
    }

    public void setHeader(Header header) {
        this.header = header;
    }

    public Body getBody() {
        return body;
    }

    public void setBody(Body body) {
        this.body = body;
    }

    public Result getResult() {
        return result;
    }

    public void setResult(Result result) {
        this.result = result;
    }

    public Header.ReturnMessage getReturn() {
        return Return;
    }

    public void setReturn(Header.ReturnMessage aReturn) {
        Return = aReturn;
    }

    @XStreamAlias("HEADER")
    public class Header {
        @XStreamAlias("MESSAGENAME")
        @XStreamAsAttribute
        String messageName;

        @XStreamAlias("TRANSACTIONID")
        @XStreamAsAttribute
        String transactionID;

        @XStreamAlias("REPLYSUBJECTNAME")
        @XStreamAsAttribute
        String replySubjectName;

        @XStreamAlias("INBOXNAME")
        @XStreamAsAttribute
        String inboxName;

        @XStreamAlias("LISTENER")
        @XStreamAsAttribute
        String listener;

        public Header() {
        }

        public String getMessageName() {
            return messageName;
        }

        public void setMessageName(String messageName) {
            this.messageName = messageName;
        }

        public String getTransactionID() {
            return transactionID;
        }

        public void setTransactionID(String transactionID) {
            this.transactionID = transactionID;
        }

        public String getReplySubjectName() {
            return replySubjectName;
        }

        public void setReplySubjectName(String replySubjectName) {
            this.replySubjectName = replySubjectName;
        }

        public String getInboxName() {
            return inboxName;
        }

        public void setInboxName(String inboxName) {
            this.inboxName = inboxName;
        }

        public String getListener() {
            return listener;
        }

        public void setListener(String listener) {
            this.listener = listener;
        }

        @XStreamAlias("RETURN")
        public class ReturnMessage {
            @XStreamAlias("RETURNCODE")
            String returnCode;
            @XStreamAlias("RETURNMESSAGE")
            String returnMessage;

            public ReturnMessage() {
            }

            public String getReturnCode() {
                return returnCode;
            }

            public void setReturnCode(String returnCode) {
                this.returnCode = returnCode;
            }

            public String getReturnMessage() {
                return returnMessage;
            }

            public void setReturnMessage(String returnMessage) {
                this.returnMessage = returnMessage;
            }
        }

    }


    @XStreamAlias("BODY")
    public class Body {
        @XStreamAlias("FACTORYNAME")
        @XStreamAsAttribute
        String factoryName;

        @XStreamAlias("MACHINENAME")
        @XStreamAsAttribute
        String machineName;

        @XStreamAlias("EVENTUSER")
        @XStreamAsAttribute
        String eventUser;

        @XStreamAlias("EVENTCOMMENT")
        @XStreamAsAttribute
        String eventComment;

        @XStreamAlias("LANGUAGE")
        @XStreamAsAttribute
        String language;

        @XStreamAlias("SOFTWAREVERSION")
        @XStreamAsAttribute
        String softwareVersion;


        @XStreamAlias("OPERATEFORMNAME")
        @XStreamAsAttribute
        String operateFormName;

        @XStreamAlias("TRANSACTIONSTARTTIME")
        @XStreamAsAttribute
        String transactionStartTime;

        @XStreamAlias("TRANSACTIONSTARTTIME2")
        @XStreamAsAttribute
        String transactionStartTime2;

        public String getFactoryName() {
            return factoryName;
        }

        public void setFactoryName(String factoryName) {
            this.factoryName = factoryName;
        }

        public String getMachineName() {
            return machineName;
        }

        public void setMachineName(String machineName) {
            this.machineName = machineName;
        }

        public String getEventUser() {
            return eventUser;
        }

        public void setEventUser(String eventUser) {
            this.eventUser = eventUser;
        }

        public String getEventComment() {
            return eventComment;
        }

        public void setEventComment(String eventComment) {
            this.eventComment = eventComment;
        }

        public String getLanguage() {
            return language;
        }

        public void setLanguage(String language) {
            this.language = language;
        }

        public String getSoftwareVersion() {
            return softwareVersion;
        }

        public void setSoftwareVersion(String softwareVersion) {
            this.softwareVersion = softwareVersion;
        }

        public String getOperateFormName() {
            return operateFormName;
        }

        public void setOperateFormName(String operateFormName) {
            this.operateFormName = operateFormName;
        }

        public String getTransactionStartTime() {
            return transactionStartTime;
        }

        public void setTransactionStartTime(String transactionStartTime) {
            this.transactionStartTime = transactionStartTime;
        }

        public String getTransactionStartTime2() {
            return transactionStartTime2;
        }

        public void setTransactionStartTime2(String transactionStartTime2) {
            this.transactionStartTime2 = transactionStartTime2;
        }
    }

    @XStreamAlias("RESULT")
    public class Result {
        @XStreamAlias("LIST")
        @XStreamAsAttribute
        List list;

        public List getList() {
            return list;
        }

        public void setList(List list) {
            this.list = list;
        }
    }

    @XStreamAlias("LIST")
    public class List {
        @XStreamAlias("LOTLIST")
        java.util.List<Lot> lotList;

        public java.util.List<Lot> getLotList() {
            return lotList;
        }

        public void setLotList(java.util.List<Lot> lotList) {
            this.lotList = lotList;
        }
    }


    @XStreamAlias("LOT")
    public class Lot {

        @XStreamAlias("LOTNAME")
        @XStreamAsAttribute
        String lotName;

        @XStreamAlias("QTAPFLAG")
        @XStreamAsAttribute
        boolean qtapFlag;

        @XStreamAlias("CARRIERNAME")
        @XStreamAsAttribute
        String carrierName;

        @XStreamAlias("PRODUCTIONTYPE")
        @XStreamAsAttribute
        String productionType;

        @XStreamAlias("PRODUCTSPECNAME")
        @XStreamAsAttribute
        String productSPECName;

        @XStreamAlias("PRODUCTSPECVERSION")
        @XStreamAsAttribute
        String productSPECVersion;


        @XStreamAlias("PRODUCTSPEC2NAME")
        @XStreamAsAttribute
        String productSPEC2Name;

        @XStreamAlias("PROCESSGROUPNAME")
        @XStreamAsAttribute
        String processGroupName;

        @XStreamAlias("PROCESSFLOWNAME")
        @XStreamAsAttribute
        String processFlowName;

        @XStreamAlias("PROCESSOPERATIONNAME")
        @XStreamAsAttribute
        boolean processOperationName;

        @XStreamAlias("SUBPRODUCTUNITQUANTITY1")
        @XStreamAsAttribute
        String subProductUnitQuantity1;

        @XStreamAlias("PRODUCTQUANTITY")
        @XStreamAsAttribute
        String productQuantity;

        @XStreamAlias("SUBPRODUCTQUANTITY")
        @XStreamAsAttribute
        String subProductQuantity;

        @XStreamAlias("SUBPRODUCTQUANTITY1")
        @XStreamAsAttribute
        String subProductQuantity1;

        @XStreamAlias("LOTGRADE")
        @XStreamAsAttribute
        String lotGroup;

        @XStreamAlias("DUEDATE")
        @XStreamAsAttribute
        String dueDate;


        @XStreamAlias("PRIORITY")
        @XStreamAsAttribute
        String priority;

        @XStreamAlias("LOTPROCESSSTATE")
        @XStreamAsAttribute
        String lotProcessState;

        @XStreamAlias("LOTHOLDSTATE")
        @XStreamAsAttribute
        String lotHoldState;

        @XStreamAlias("REWORKSTATE")
        @XStreamAsAttribute
        String reworkState;

        @XStreamAlias("REWORKCOUNT")
        @XStreamAsAttribute
        String reworkCount;

        @XStreamAlias("MACHINENAME")
        @XStreamAsAttribute
        String machineName;

        @XStreamAlias("PORTNAME")
        @XStreamAsAttribute
        String portName;

        @XStreamAlias("OWNERID")
        @XStreamAsAttribute
        String ownerID;

        @XStreamAlias("LASTEVENTCOMMENT")
        @XStreamAsAttribute
        String lastEventComment;

        @XStreamAlias("PROCESSOPERATIONDESCRIPTION")
        @XStreamAsAttribute
        boolean processOperationDescription;

        @XStreamAlias("TIMEUSED")
        @XStreamAsAttribute
        String timeUsed;

        @XStreamAlias("FACTORYNAME")
        @XStreamAsAttribute
        String factoryName;

        @XStreamAlias("LASTCLEANTIME")
        @XStreamAsAttribute
        String lastCleanTime;

        @XStreamAlias("DURATIONUSEDLIMIT")
        @XStreamAsAttribute
        String durationUsrdLimit;

        @XStreamAlias("GROUPNAME")
        @XStreamAsAttribute
        String groupName;

        @XStreamAlias("SEQUENCE")
        @XStreamAsAttribute
        String sequence;


        @XStreamAlias("RESERVEDMACHINENAME")
        @XStreamAsAttribute
        String reservedMachineName;

        @XStreamAlias("LASTLOGGEDOUTTIME")
        @XStreamAsAttribute
        String lastLoggedOutTime;

        @XStreamAlias("STAYHOUR")
        @XStreamAsAttribute
        String stayhour;

        @XStreamAlias("SHIPGROUPNAME")
        @XStreamAsAttribute
        String shipGroupName;

        @XStreamAlias("CARRIERLOCATION")
        @XStreamAsAttribute
        String carrierLocation;

        @XStreamAlias("TRANSFERSTATE")
        @XStreamAsAttribute
        String transferState;

        @XStreamAlias("DISPATCHINGFLAG")
        @XStreamAsAttribute
        boolean dispatchingFlag;

        @XStreamAlias("QTAPGROUPNAME")
        @XStreamAsAttribute
        String qtapGroupName;

        @XStreamAlias("SORTGRADE")
        @XStreamAsAttribute
        String sortGrade;

        @XStreamAlias("TECNRECIPEENABLE")
        @XStreamAsAttribute
        boolean tecnRecipeEnable;

        @XStreamAlias("MACHINERECIPENAME")
        @XStreamAsAttribute
        String machineRecipeName;

        @XStreamAlias("PROCESSENABLE")
        @XStreamAsAttribute
        boolean processEnable;

        @XStreamAlias("QTIMEOVER")
        @XStreamAsAttribute
        String qtimeOver;

        @XStreamAlias("SKIPFLAG")
        @XStreamAsAttribute
        boolean skipFlag;

        public String getLotName() {
            return lotName;
        }

        public void setLotName(String lotName) {
            this.lotName = lotName;
        }

        public boolean isQtapFlag() {
            return qtapFlag;
        }

        public void setQtapFlag(boolean qtapFlag) {
            this.qtapFlag = qtapFlag;
        }

        public String getCarrierName() {
            return carrierName;
        }

        public void setCarrierName(String carrierName) {
            this.carrierName = carrierName;
        }

        public String getProductionType() {
            return productionType;
        }

        public void setProductionType(String productionType) {
            this.productionType = productionType;
        }

        public String getProductSPECName() {
            return productSPECName;
        }

        public void setProductSPECName(String productSPECName) {
            this.productSPECName = productSPECName;
        }

        public String getProductSPECVersion() {
            return productSPECVersion;
        }

        public void setProductSPECVersion(String productSPECVersion) {
            this.productSPECVersion = productSPECVersion;
        }

        public String getProductSPEC2Name() {
            return productSPEC2Name;
        }

        public void setProductSPEC2Name(String productSPEC2Name) {
            this.productSPEC2Name = productSPEC2Name;
        }

        public String getProcessGroupName() {
            return processGroupName;
        }

        public void setProcessGroupName(String processGroupName) {
            this.processGroupName = processGroupName;
        }

        public String getProcessFlowName() {
            return processFlowName;
        }

        public void setProcessFlowName(String processFlowName) {
            this.processFlowName = processFlowName;
        }

        public boolean isProcessOperationName() {
            return processOperationName;
        }

        public void setProcessOperationName(boolean processOperationName) {
            this.processOperationName = processOperationName;
        }

        public String getSubProductUnitQuantity1() {
            return subProductUnitQuantity1;
        }

        public void setSubProductUnitQuantity1(String subProductUnitQuantity1) {
            this.subProductUnitQuantity1 = subProductUnitQuantity1;
        }

        public String getProductQuantity() {
            return productQuantity;
        }

        public void setProductQuantity(String productQuantity) {
            this.productQuantity = productQuantity;
        }

        public String getSubProductQuantity() {
            return subProductQuantity;
        }

        public void setSubProductQuantity(String subProductQuantity) {
            this.subProductQuantity = subProductQuantity;
        }

        public String getSubProductQuantity1() {
            return subProductQuantity1;
        }

        public void setSubProductQuantity1(String subProductQuantity1) {
            this.subProductQuantity1 = subProductQuantity1;
        }

        public String getLotGroup() {
            return lotGroup;
        }

        public void setLotGroup(String lotGroup) {
            this.lotGroup = lotGroup;
        }

        public String getDueDate() {
            return dueDate;
        }

        public void setDueDate(String dueDate) {
            this.dueDate = dueDate;
        }

        public String getPriority() {
            return priority;
        }

        public void setPriority(String priority) {
            this.priority = priority;
        }

        public String getLotProcessState() {
            return lotProcessState;
        }

        public void setLotProcessState(String lotProcessState) {
            this.lotProcessState = lotProcessState;
        }

        public String getLotHoldState() {
            return lotHoldState;
        }

        public void setLotHoldState(String lotHoldState) {
            this.lotHoldState = lotHoldState;
        }

        public String getReworkState() {
            return reworkState;
        }

        public void setReworkState(String reworkState) {
            this.reworkState = reworkState;
        }

        public String getReworkCount() {
            return reworkCount;
        }

        public void setReworkCount(String reworkCount) {
            this.reworkCount = reworkCount;
        }

        public String getMachineName() {
            return machineName;
        }

        public void setMachineName(String machineName) {
            this.machineName = machineName;
        }

        public String getPortName() {
            return portName;
        }

        public void setPortName(String portName) {
            this.portName = portName;
        }

        public String getOwnerID() {
            return ownerID;
        }

        public void setOwnerID(String ownerID) {
            this.ownerID = ownerID;
        }

        public String getLastEventComment() {
            return lastEventComment;
        }

        public void setLastEventComment(String lastEventComment) {
            this.lastEventComment = lastEventComment;
        }

        public boolean isProcessOperationDescription() {
            return processOperationDescription;
        }

        public void setProcessOperationDescription(boolean processOperationDescription) {
            this.processOperationDescription = processOperationDescription;
        }

        public String getTimeUsed() {
            return timeUsed;
        }

        public void setTimeUsed(String timeUsed) {
            this.timeUsed = timeUsed;
        }

        public String getFactoryName() {
            return factoryName;
        }

        public void setFactoryName(String factoryName) {
            this.factoryName = factoryName;
        }

        public String getLastCleanTime() {
            return lastCleanTime;
        }

        public void setLastCleanTime(String lastCleanTime) {
            this.lastCleanTime = lastCleanTime;
        }

        public String getDurationUsrdLimit() {
            return durationUsrdLimit;
        }

        public void setDurationUsrdLimit(String durationUsrdLimit) {
            this.durationUsrdLimit = durationUsrdLimit;
        }

        public String getGroupName() {
            return groupName;
        }

        public void setGroupName(String groupName) {
            this.groupName = groupName;
        }

        public String getSequence() {
            return sequence;
        }

        public void setSequence(String sequence) {
            this.sequence = sequence;
        }

        public String getReservedMachineName() {
            return reservedMachineName;
        }

        public void setReservedMachineName(String reservedMachineName) {
            this.reservedMachineName = reservedMachineName;
        }

        public String getLastLoggedOutTime() {
            return lastLoggedOutTime;
        }

        public void setLastLoggedOutTime(String lastLoggedOutTime) {
            this.lastLoggedOutTime = lastLoggedOutTime;
        }

        public String getStayhour() {
            return stayhour;
        }

        public void setStayhour(String stayhour) {
            this.stayhour = stayhour;
        }

        public String getShipGroupName() {
            return shipGroupName;
        }

        public void setShipGroupName(String shipGroupName) {
            this.shipGroupName = shipGroupName;
        }

        public String getCarrierLocation() {
            return carrierLocation;
        }

        public void setCarrierLocation(String carrierLocation) {
            this.carrierLocation = carrierLocation;
        }

        public String getTransferState() {
            return transferState;
        }

        public void setTransferState(String transferState) {
            this.transferState = transferState;
        }

        public boolean isDispatchingFlag() {
            return dispatchingFlag;
        }

        public void setDispatchingFlag(boolean dispatchingFlag) {
            this.dispatchingFlag = dispatchingFlag;
        }

        public String getQtapGroupName() {
            return qtapGroupName;
        }

        public void setQtapGroupName(String qtapGroupName) {
            this.qtapGroupName = qtapGroupName;
        }

        public String getSortGrade() {
            return sortGrade;
        }

        public void setSortGrade(String sortGrade) {
            this.sortGrade = sortGrade;
        }

        public boolean isTecnRecipeEnable() {
            return tecnRecipeEnable;
        }

        public void setTecnRecipeEnable(boolean tecnRecipeEnable) {
            this.tecnRecipeEnable = tecnRecipeEnable;
        }

        public String getMachineRecipeName() {
            return machineRecipeName;
        }

        public void setMachineRecipeName(String machineRecipeName) {
            this.machineRecipeName = machineRecipeName;
        }

        public boolean isProcessEnable() {
            return processEnable;
        }

        public void setProcessEnable(boolean processEnable) {
            this.processEnable = processEnable;
        }

        public String getQtimeOver() {
            return qtimeOver;
        }

        public void setQtimeOver(String qtimeOver) {
            this.qtimeOver = qtimeOver;
        }

        public boolean isSkipFlag() {
            return skipFlag;
        }

        public void setSkipFlag(boolean skipFlag) {
            this.skipFlag = skipFlag;
        }
    }

}
