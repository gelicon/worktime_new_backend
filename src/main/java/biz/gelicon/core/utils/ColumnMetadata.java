package biz.gelicon.core.utils;

import javax.persistence.Column;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

/**
 * Метаданные колонки для {@link TableMetadata#columnMetadataList}
 */
public class ColumnMetadata {
    public enum ColumnMetaKind {
        COLUMN,
        JOINLIST
    }

    String columnName; // Имя поля в базе данных
    String columnDefinition; // Русское описание таблицы
    Boolean nullable; // Возможность хранить NULL
    Boolean idFlag; // флаг ИД
    Field field; // Java поле объекта
    Method methodSet; // Метод put для поля
    Method methodGet; // Метод get для поля
    ColumnMetaKind metaKind = ColumnMetaKind.COLUMN;

    String joinTableName; // Имя таблицы для соединения
    String referencedColumnName; // Имя FK в таблице для соединения (если нет, то используется Id)

    public String getColumnName() {
        return this.columnName;
    }

    public void setColumnName(String columnName) {
        this.columnName = columnName;
    }

    public String getColumnDefinition() {
        return this.columnDefinition;
    }

    public void setColumnDefinition(String columnDefinition) {
        this.columnDefinition = columnDefinition;
    }

    public Boolean getNullable() {
        return this.nullable;
    }

    public void setNullable(Boolean nullable) {
        this.nullable = nullable;
    }

    public Field getField() {
        return this.field;
    }

    public void setField(Field field) {
        this.field = field;
    }

    public Boolean getIdFlag() {
        return this.idFlag;
    }

    public void setIdFlag(Boolean idFlag) {
        this.idFlag = idFlag;
    }

    public Method getMethodGet() {
        return methodGet;
    }

    public void setMethodGet(Method methodGet) {
        this.methodGet = methodGet;
    }

    public Method getMethodSet() {
        return methodSet;
    }

    public void setMethodSet(Method methodSet) {
        this.methodSet = methodSet;
    }

    public String getJoinTableName() {
        return joinTableName;
    }

    public void setJoinTableName(String joinTableName) {
        this.joinTableName = joinTableName;
    }

    public String getReferencedColumnName() {
        return referencedColumnName;
    }

    public void setReferencedColumnName(String referencedColumnName) {
        this.referencedColumnName = referencedColumnName;
    }

    public ColumnMetaKind getMetaKind() {
        return metaKind;
    }

    public void setMetaKind(ColumnMetaKind metaKind) {
        this.metaKind = metaKind;
    }
}
