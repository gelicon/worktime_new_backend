package biz.gelicon.core.reports;

import biz.gelicon.services.ReportParamOptions;

public class OptionParam implements ReportParamOptions {
    private boolean nulleable;

    public OptionParam(boolean nulleable) {
        this.nulleable = nulleable;
    }

    public boolean isNulleable() {
        return nulleable;
    }
}
