package biz.gelicon.core.dto;

public class SelectDisplayData<K> {
    private K value;

    private String title;

    public SelectDisplayData() {
    }

    public SelectDisplayData(K value, String title) {
        this.value = value;
        this.title = title;
    }

    public K getValue() {
        return value;
    }

    public void setValue(K value) {
        this.value = value;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

}
