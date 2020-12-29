package com.chot.entity.messageEntity;


import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamAsAttribute;

import java.util.ArrayList;
import java.util.List;

@XStreamAlias("MESSAGE")
public class CheckRecipeParameterRequest {
    @XStreamAlias("HEADER")//别名，在xml中的名字
    @XStreamAsAttribute//如果是属性值，就加标识
            Header header;

    @XStreamAlias("BODY")
    @XStreamAsAttribute
    Body body;

    @XStreamAlias("RETURN")
    @XStreamAsAttribute
    Header.ReturnMessage Return;

    public CheckRecipeParameterRequest() {
    }


    public Header getHeader() {
        return header;
    }

    public void setHeader(Header header) {
        this.header = header;
    }

    public Header.ReturnMessage getReturn() {
        return Return;
    }

    public void setReturn(Header.ReturnMessage aReturn) {
        Return = aReturn;
    }

    public Body getBody() {
        return body;
    }

    public void setBody(Body body) {
        this.body = body;
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

        @XStreamAlias("LINENAME")
        @XStreamAsAttribute
        String lineName;

        @XStreamAlias("LINE_RECIPE_LIST")
//        @XStreamImplicit(itemFieldName = "LINE_RECIPE")//隐式标识，如果没有标注list,就用这个注解
                List<LineRecipe> LINE_RECIPE_LIST;

        @XStreamAlias("OPERATORID")
        @XStreamAsAttribute
        String operatorID;

        @XStreamAlias("TRANSACTIONSTARTTIME")
        @XStreamAsAttribute
        String transactionStartTime;

        public Body() {
            getLineRecipeList();
        }

        public String getLineName() {
            return lineName;
        }

        public void setLineName(String lineName) {
            this.lineName = lineName;
        }

        public List<LineRecipe> getLineRecipeList() {
            if (LINE_RECIPE_LIST == null) {
                LINE_RECIPE_LIST = new ArrayList<>();
            }
            return LINE_RECIPE_LIST;
        }

        public void setLineRecipeList(List<LineRecipe> lineRecipeList) {
            this.LINE_RECIPE_LIST = lineRecipeList;
        }

        public String getOperatorID() {
            return operatorID;
        }

        public void setOperatorID(String operatorID) {
            this.operatorID = operatorID;
        }

        public String getTransactionStartTime() {
            return transactionStartTime;
        }

        public void setTransactionStartTime(String transactionStartTime) {
            this.transactionStartTime = transactionStartTime;
        }
    }


    @XStreamAlias("LINE_RECIPE")
    public static class LineRecipe {
        @XStreamAlias("RECIPEEVENTTYPE")
        String recipeEventType;
        @XStreamAlias("LINERECIPENAME")
        String lineRecipeName;
        @XStreamAlias("TIMEOUT_UNIT_LIST")
        List<TimeOutUnit> TIMEOUT_UNIT_LIST;
        @XStreamAlias("CIMOFF_UNIT_LIST")
        List<CimoffUnit> CIMOFF_UNIT_LIST;
        @XStreamAlias("UNIT_LIST")
        List<Unit> UNIT_LIST;

        public LineRecipe() {
            getUnitList();
        }


        public String getRecipeEventType() {
            return recipeEventType;
        }

        public void setRecipeEventType(String recipeEventType) {
            this.recipeEventType = recipeEventType;
        }

        public String getLineRecipeName() {
            return lineRecipeName;
        }

        public void setLineRecipeName(String lineRecipeName) {
            this.lineRecipeName = lineRecipeName;
        }

        public List<TimeOutUnit> getTimeOutUnits() {
            if (TIMEOUT_UNIT_LIST == null) {
                TIMEOUT_UNIT_LIST = new ArrayList<>();
            }
            return TIMEOUT_UNIT_LIST;
        }

        public void setTimeOutUnits(List<TimeOutUnit> timeOutUnits) {
            this.TIMEOUT_UNIT_LIST = timeOutUnits;
        }

        public List<CimoffUnit> getCimoffUnits() {
            if (CIMOFF_UNIT_LIST == null) {
                CIMOFF_UNIT_LIST = new ArrayList<>();
            }
            return CIMOFF_UNIT_LIST;
        }

        public void setCimoffUnits(List<CimoffUnit> cimoffUnits) {
            this.CIMOFF_UNIT_LIST = cimoffUnits;
        }

        public List<Unit> getUnitList() {
            if (UNIT_LIST == null) {
                UNIT_LIST = new ArrayList<>();
            }
            return UNIT_LIST;
        }

        public void setUnitList(List<Unit> unitList) {
            this.UNIT_LIST = unitList;
        }

    }


    @XStreamAlias("UNIT")
    public class Unit {
        @XStreamAlias("UNITNAME")
        @XStreamAsAttribute
        String unitName;

        @XStreamAlias("UNITRECIPENAME")
        @XStreamAsAttribute
        String unitRecipeName;

        @XStreamAlias("RECIPEPARALIST")
        List<Parameter> recipeparaList;

        public Unit() {
            getRecipeparaList();
        }


        public List<Parameter> getRecipeparaList() {
            if (recipeparaList == null) {
                recipeparaList = new ArrayList<>();
            }
            return recipeparaList;
        }

        public void setRecipeparaList(List<Parameter> recipeparaList) {
            this.recipeparaList = recipeparaList;
        }

    }

    @XStreamAlias("PARA")
    public class Parameter {
        @XStreamAlias("TRACELEVEL")
        String tracelevel;
        @XStreamAlias("PARANAME")
        String paraName;
        @XStreamAlias("VALUETYPE")
        String valueType;
        @XStreamAlias("PARAVALUE")
        String paraValue;

        public Parameter() {
        }

        public Parameter(String tracelevel, String paraName, String valueType, String paraValue) {
            this.tracelevel = tracelevel;
            this.paraName = paraName;
            this.valueType = valueType;
            this.paraValue = paraValue;
        }

        public String getTracelevel() {
            return tracelevel;
        }

        public void setTracelevel(String tracelevel) {
            this.tracelevel = tracelevel;
        }

        public String getParaName() {
            return paraName;
        }

        public void setParaName(String paraName) {
            this.paraName = paraName;
        }

        public String getValueType() {
            return valueType;
        }

        public void setValueType(String valueType) {
            this.valueType = valueType;
        }

        public String getParaValue() {
            return paraValue;
        }

        public void setParaValue(String paraValue) {
            this.paraValue = paraValue;
        }
    }

    @XStreamAlias("TIMEOUT_UNIT")
    public class TimeOutUnit {
    }

    @XStreamAlias("CIMOFF_UNIT")
    public class CimoffUnit {
    }

}
