package biz.gelicon.core.reports;

import biz.gelicon.services.OptionForSelectParam;
import biz.gelicon.services.ReportParamOptions;

import java.util.List;

public class OptionSelect extends OptionParam implements ReportParamOptions {
    private List<OptionForSelectParam> values;
    private String uri;
    private String dataForPost;
    private boolean cashable;
    private String valueName;
    private String displayValueName;

    public OptionSelect(String uri, String dataForPost, String valueName, String displayValueName, boolean nulleable, boolean cashable) {
        super(nulleable);
        this.uri = uri;
        this.dataForPost = dataForPost;
        this.cashable = cashable;
        this.valueName = valueName;
        this.displayValueName = displayValueName;
    }

    public OptionSelect(List<OptionForSelectParam> values, boolean nulleable) {
        super(nulleable);
        this.values=values;
    }

    public String getUri() {
        return uri;
    }

    public String getDataForPost() {
        return dataForPost;
    }

    public boolean isCashable() {
        return cashable;
    }

    public String getValueName() {
        return valueName;
    }

    public String getDisplayValueName() {
        return displayValueName;
    }

    public List<OptionForSelectParam> getValues() {
        return values;
    }
}
