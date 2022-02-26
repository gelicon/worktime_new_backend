package biz.gelicon.core.dto;

public class SelectDisplayDataExt<A, K> extends SelectDisplayData<K>{
    private A additional;

    public SelectDisplayDataExt() {
    }

    public SelectDisplayDataExt(K value, String title, A additional) {
        super(value, title);
        this.additional = additional;
    }

    public A getAdditional() {
        return additional;
    }

    public void setAdditional(A additional) {
        this.additional = additional;
    }
}
