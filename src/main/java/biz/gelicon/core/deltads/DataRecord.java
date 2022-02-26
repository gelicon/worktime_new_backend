package biz.gelicon.core.deltads;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.ArrayList;
import java.util.List;

@Schema(description = "Запись об изменении данных")
public class DataRecord<T> {
    @Schema(description = "Момент изменения")
    private Long timeLabel;
    @Schema(description = "Тип изменения")
    @JsonFormat(shape = JsonFormat.Shape.NUMBER_INT)
    private StatusRecord status;
    @Schema(description = "Измененные данные. " +
            "В случае вставки - полный набор полей. " +
            "В случае изменения - полный набор полей. " +
            "В случае удаления - идентификатор и прикладной номер позиции (для возможного сообщения пользователю).")
    private T record;
    @Schema(description = "Данные до изменения. Поле имеет значение при операции Изменение")
    private T oldRecord;

    public static <R> DataRecord<R> newRecord() {
        DataRecord<R> rec = new DataRecord();
        rec.status = StatusRecord.ORIGINAL;
        rec.timeLabel = System.currentTimeMillis();
        return rec;
    }

    public StatusRecord getStatus() {
        return status;
    }

    public void setStatus(StatusRecord status) {
        this.status = status;
    }

    public T getRecord() {
        return record;
    }

    public void setRecord(T record) {
        this.record = record;
    }

    public Long getTimeLabel() {
        return timeLabel;
    }

    public void setTimeLabel(Long timeLabel) {
        this.timeLabel = timeLabel;
    }

    public T getOldRecord() {
        return oldRecord;
    }

    public void setOldRecord(T oldRecord) {
        this.oldRecord = oldRecord;
    }
}
