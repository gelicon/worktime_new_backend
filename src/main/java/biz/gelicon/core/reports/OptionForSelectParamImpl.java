package biz.gelicon.core.reports;

public class OptionForSelectParamImpl implements biz.gelicon.services.OptionForSelectParam {
    private Integer value;
    private String displayText;

    public OptionForSelectParamImpl(Integer value, String displayText) {
        this.value = value;
        this.displayText = displayText;
    }

    @Override
    public Integer getValue() {
        return value;
    }

    @Override
    public String getDisplayText() {
        return displayText;
    }
}
