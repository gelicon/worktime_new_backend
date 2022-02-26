package biz.gelicon.core.deltads;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.*;
import java.util.stream.Stream;

@Schema(description = "Контейнер данных с их изменениями")
public class MemoryDataSet<T> {
    @Schema(description = "Данные")
    private List<T> data;
    @Schema(description = "Изменения в данных")
    private List<DataRecord<T>> delta;

    public MemoryDataSet() {
        this.data = new ArrayList<>();
    }

    public MemoryDataSet(List<T> data) {
        this.data = data;
    }

    public void forEachDelta(DataSetOperation<T> op) {
        if(delta!=null) {
            Map<Integer,RuntimeException> pendingException = new HashMap<>();
            delta.stream()
                    .sorted((dr1,dr2)->dr1.getTimeLabel().compareTo(dr2.getTimeLabel()))
                    .forEach(dr-> {
                        Integer posNumber = ((RecordInfo) dr.getRecord()).getPositionNumber();
                        try {
                            op.process(dr);
                            switch(dr.getStatus()) {
                                case UPDATED:
                                    // сбрасываем все отложенные ИС.
                                    // Номер берем до изменения, так как он может поменятся
                                    Integer oldNumber = ((RecordInfo) dr.getOldRecord()).getPositionNumber();
                                    pendingException.remove(oldNumber);
                                    break;
                                case DELETED:
                                    // сбрасываем все отложенные ИС
                                    pendingException.remove(posNumber);
                                    break;
                            }
                        } catch (Exception ex) {
                            RuntimeException exPos = new RuntimeException(String.format("Ошибка в позиции №%d:", posNumber) + ex.getMessage(), ex);
                            pendingException.put(posNumber,exPos);
                            // удаление не откладывается
                            if(dr.getStatus() == StatusRecord.DELETED) {
                                throw exPos;
                            }
                        }
                    });
            // возбуждаем ИС
            if(!pendingException.keySet().isEmpty()) {
                pendingException.entrySet().forEach(entry->{
                    throw entry.getValue();
                });
            }
        }
    }

    public Stream<DataRecord<T>> withNumber(Integer posNumber) {
        if(delta!=null) {
            return delta.stream()
                .filter(dr->((RecordInfo) dr.getRecord()).getPositionNumber().equals(posNumber));
        } else {
            return Stream.of();
        }
    }

    public List<T> getData() {
        return data;
    }

    public void setData(List<T> data) {
        this.data = data;
    }

    public List<DataRecord<T>> getDelta() {
        if(delta==null) {
            return Collections.unmodifiableList(List.of());
        }
        return Collections.unmodifiableList(delta);
    }

    public void setDelta(List<DataRecord<T>> delta) {
        this.delta = delta;
    }

}
