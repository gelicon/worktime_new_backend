package biz.gelicon.core.reports;

import biz.gelicon.services.ReportParamOptions;

public class OptionDataLookup extends OptionParam implements ReportParamOptions {
    private String uri;
    private String dataForPost;

    public OptionDataLookup(String uri, String dataForPost, boolean nulleable) {
        super(nulleable);
        this.uri = uri;
        this.dataForPost = dataForPost;
    }

    public String getUri() {
        return uri;
    }

    public String getDataForPost() {
        return dataForPost;
    }
}
