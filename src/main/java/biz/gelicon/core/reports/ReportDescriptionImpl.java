package biz.gelicon.core.reports;


import biz.gelicon.services.ReportDescription;
import biz.gelicon.services.ReportParamOptions;
import biz.gelicon.services.ReportService;

import java.util.*;

public class ReportDescriptionImpl implements ReportDescription {
    private final String code;
    private final String name;
    private String unitName;
    private String entityName;

    private Map<String,ParamDesc> params = new HashMap<>();
    // это порядок следования - по умолчанию в порядке регистрации
    private List<String> paramsOrder = new ArrayList<>();

    private ReportService holder;


    public ReportDescriptionImpl(String code, String name) {
        this.code = code;
        this.name = name;
    }

    public String getCode() {
        return code;
    }

    public String getName() {
        return name;
    }

    public String getUnitName() {
        return unitName;
    }

    public String getEntityName() {
        return entityName;
    }

    public Map<String, ParamDesc> getParams() {
        return params;
    }

    @Override
    public void forUnit(String unitName) {
        this.unitName = unitName;
    }

    @Override
    public void forEntity(String entityName) {
        this.entityName = entityName;
    }

    @Override
    public void declareParam(String pname,String plabel, ParamType paramType, ParamSubType paramSubType) {
        params.put(pname,new ParamDesc(pname,plabel,paramType,paramSubType));
        paramsOrder.add(pname);
    }

    @Override
    public void declareParam(String pname, String plabel, ParamType paramType) {
        params.put(pname,new ParamDesc(pname,plabel,paramType,ParamSubType.None));
        paramsOrder.add(pname);
    }

    @Override
    public void declareOptionsForParam(String pname, ReportParamOptions o) {
        ParamDesc desc = getParam(pname);
        desc.options = o;
    }

    @Override
    public void paramInitValue(String pname, int i) {
        ParamDesc desc = getParam(pname);
        desc.initValue = new InitValue(Integer.valueOf(i));
    }

    @Override
    public void paramInitValue(String pname, int value, String displayText) {
        ParamDesc desc = getParam(pname);
        desc.initValue = new InitValue(new OptionForSelectParamImpl(value,displayText));
    }

    @Override
    public void paramInitValue(String pname, boolean b) {
        ParamDesc desc = getParam(pname);
        desc.initValue = new InitValue(Boolean.valueOf(b));
    }

    @Override
    public void paramInitValue(String pname, String s) {
        ParamDesc desc = getParam(pname);
        desc.initValue = new InitValue(s);
    }

    @Override
    public void paramInitValue(String pname, Date date) {
        ParamDesc desc = getParam(pname);
        desc.initValue = new InitValue(date.getTime());
    }

    @Override
    public void paramInitValue(String pname, Date datebeg, Date dateend) {
        ParamDesc desc = getParam(pname);
        desc.initValue =  new InitRangeValue(datebeg,dateend);
    }

    private ParamDesc getParam(String pname) {
        ParamDesc desc = params.get(pname);
        if(desc==null)
            throw new ReportParamNotFound(name,pname);
        return desc;
    }

    public void setHolder(ReportService currService) {
        this.holder = currService;
    }

    public ReportService getHolder() {
        return holder;
    }

    public List<String> getParamsOrder() {
        return paramsOrder;
    }

    public static class ParamDesc{
        private String name;
        private String label;
        private ParamType type;
        private ParamSubType subType;
        private Object initValue;
        private Object options;

        public ParamDesc(String name, String label, ParamType type, ParamSubType subType) {
            this.name = name;
            this.label = label;
            this.type = type;
            this.subType = subType;
        }

        public String getName() {
            return name;
        }

        public ParamType getType() {
            return type;
        }

        public ParamSubType getSubType() {
            return subType;
        }

        public Object getInitValue() {
            return initValue;
        }

        public Object getOptions() {
            return options;
        }

        public String getLabel() {
            return label;
        }
    }

    public static class InitValue{
        public Object value;

        public InitValue(Object value) {
            this.value = value;
        }

        public Object getValue() {
            return value;
        }

        public void setValue(Object value) {
            this.value = value;
        }
    }

    public static class InitRangeValue{
        public Object valueStart;
        public Object valueFinish;

        public InitRangeValue(Object valueStart, Object valueFinish) {
            this.valueStart = valueStart;
            this.valueFinish = valueFinish;
        }

        public Object getValueStart() {
            return valueStart;
        }

        public void setValueStart(Object valueStart) {
            this.valueStart = valueStart;
        }

        public Object getValueFinish() {
            return valueFinish;
        }

        public void setValueFinish(Object valueFinish) {
            this.valueFinish = valueFinish;
        }

    }

    public static class ReportParamNotFound extends RuntimeException {
        public ReportParamNotFound(String name, String pname) {
            super(String.format("Param %s not found in report %s",pname,name));
        }
    }

}
