package biz.gelicon.core.response;

public class ReportResponse {
    private String url;
    private String nameResult;

    public ReportResponse() {
    }

    public ReportResponse(String url, String nameResult) {
        this.url = url;
        this.nameResult = nameResult;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getNameResult() {
        return nameResult;
    }

    public void setNameResult(String nameResult) {
        this.nameResult = nameResult;
    }
}
