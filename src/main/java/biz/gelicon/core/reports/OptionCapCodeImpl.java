package biz.gelicon.core.reports;


import biz.gelicon.services.ReportParamOptions;

public class OptionCapCodeImpl extends OptionParam implements ReportParamOptions {
    private Integer capCodeTypeId;
    private boolean cashable;
    private boolean multiple;

    public OptionCapCodeImpl(Integer capCodeTypeId, boolean nulleable, boolean cashable) {
        this(capCodeTypeId,nulleable,cashable,false);
    }

    public OptionCapCodeImpl(Integer capCodeTypeId, boolean nulleable, boolean cashable, boolean multiple) {
        super(nulleable);
        this.capCodeTypeId = capCodeTypeId;
        this.cashable = cashable;
        this.multiple = multiple;
    }

    public Integer getCapCodeTypeId() {
        return capCodeTypeId;
    }

    public boolean isCashable() {
        return cashable;
    }

    public boolean isMultiple() {
        return multiple;
    }
}
