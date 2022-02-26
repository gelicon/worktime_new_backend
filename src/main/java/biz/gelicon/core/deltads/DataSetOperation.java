package biz.gelicon.core.deltads;

public interface DataSetOperation<T> {
    public void process(DataRecord<T> record);
}
