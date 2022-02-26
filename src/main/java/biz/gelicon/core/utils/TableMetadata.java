package biz.gelicon.core.utils;

import biz.gelicon.core.annotations.NoRegister;
import biz.gelicon.core.annotations.OnlyEdition;
import biz.gelicon.core.config.Config;
import biz.gelicon.core.repository.TableRepository;
import org.reflections.Reflections;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.Column;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.Table;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Метаданные модели, полученные из аннотаций @Table @Id @Column и др.
 *
 * @author dav
 */
public class TableMetadata {

    String tableName; // Имя таблицы в базе данных
    Class modelCls;
    Field idField; // Первичный ключ
    String idFieldName; // Имя поля первичного ключа
    List<ColumnMetadata> columnMetadataList = new ArrayList<>(); // Коллекция полей таблицы
    List<ColumnMetadata> joinColumnMetadataList  = new ArrayList<>();

    public String getTableName() {
        return this.tableName;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    public String getIdFieldName() {
        return this.idFieldName;
    }

    public void setIdFieldName(String idFieldName) {
        this.idFieldName = idFieldName;
    }

    public Field getIdField() {
        return this.idField;
    }

    public void setIdField(Field idField) {
        this.idField = idField;
    }

    public Class getModelCls() {
        return modelCls;
    }

    public void setModelCls(Class modelCls) {
        this.modelCls = modelCls;
    }

    private static final Logger logger = LoggerFactory.getLogger(TableMetadata.class);

    /**
     * Получает все поля из аннотаций по классу репозитория <p> Для модели получает класс дженерика
     * объекта
     *
     * @param o класс модели
     */
    public void loadTableMetadata(Object o) {
        // Получим класс дженерика интерфейса аннотированного как @Table
        Class cls = OrmUtils.getClassGenericInterfaceAnnotationTable(o.getClass());
        loadTableMetadata(cls);
    }

    public List<ColumnMetadata> getColumnMetadataList() {
        return this.columnMetadataList;
    }

    public List<ColumnMetadata> getJoinColumnMetadataList() {
        return joinColumnMetadataList;
    }

    public String getColumnNameByFieldName(final String name) {
        return getColumnMetadataList().stream()
                .filter(c -> c.getField().getName().equals(name))
                .map(ColumnMetadata::getColumnName)
                .findAny()
                .orElse(null);
    }

    /**
     * Получает все поля из аннотаций по классу модели
     *
     * @param cls класс модели
     */
    public void loadTableMetadata(Class cls) {

        // весь класс только дял определенных редакций
        if (cls.isAnnotationPresent(OnlyEdition.class) &&
               !Arrays.asList(((OnlyEdition)cls.getAnnotation(OnlyEdition.class)).tags())
                        .stream()
                        .anyMatch(ed -> ed == Config.CURRENT_EDITION_TAG)) {
            return;
        }

        // Получим и запишем имя таблицы
        setTableName(OrmUtils.getTableName(cls));
        // Запишем класс модели
        setModelCls(cls);
        // Найдем поле - первичный ключ и запишем
        setIdField(OrmUtils.getIdField(cls));
        if (getIdField() == null) {
            throw new RuntimeException(
                    "Таблица " + tableName + " не имеет поля, аннотированного как @Id.");
        }
        // Запишем имя поля первичного ключа
        setIdFieldName(OrmUtils.getColumnName(idField));
        // Ищем все методы класса
        Map<String, Method> methodMap = new HashMap<>();
        List<Method> methods = new ArrayList<>();
        methods = Arrays.asList(cls.getMethods());
        for (int i = 0; i < methods.size(); i++) {
            methodMap.put(methods.get(i).getName(), methods.get(i));
        }

        // Ищем все поля помеченные аннотацией @Column
        List<Field> fieldList = Arrays.stream(cls.getDeclaredFields())
                .filter(c -> c.isAnnotationPresent(Column.class))   // Проверим аннотацию Column
                .filter(c ->                                        // Проверим на соответствие редакции
                        !c.isAnnotationPresent(OnlyEdition.class) ||
                                Arrays.asList(c.getAnnotation(OnlyEdition.class).tags()).stream()
                                    .anyMatch(ed->ed== Config.CURRENT_EDITION_TAG))
                .collect(Collectors.toList());
        // Записываем их все в коллекцию
        for (Field field : fieldList) {
            addColumnToMetaData(methodMap, field);
        }
        // Ищем все поля помеченные аннотацией @JoinColumn
        List<Field> joinFieldList = Arrays.stream(cls.getDeclaredFields())
                .filter(c -> c.isAnnotationPresent(JoinColumn.class)) // Проверим аннотацию JoinColumn
                .filter(c ->                                        // Проверим на соответствие редакции
                        !c.isAnnotationPresent(OnlyEdition.class) ||
                                Arrays.asList(c.getAnnotation(OnlyEdition.class).tags()).stream()
                                        .anyMatch(ed->ed== Config.CURRENT_EDITION_TAG))
                .collect(Collectors.toList());
        for (Field field : joinFieldList) {
            // join для списков
            if(Collection.class.isAssignableFrom(field.getType())) {
                ColumnMetadata meta = addJoinColumnToMetaData(methodMap, field);
                meta.setMetaKind(ColumnMetadata.ColumnMetaKind.JOINLIST);
            }
        }


    }

    private ColumnMetadata addJoinColumnToMetaData(Map<String, Method> methodMap, Field field) {
        ColumnMetadata columnMetadata = new ColumnMetadata();
        JoinColumn anColumn = field.getAnnotation(JoinColumn.class);
        columnMetadata.setColumnName(anColumn.name());
        columnMetadata.setJoinTableName(anColumn.table());
        columnMetadata.setField(field);
        columnMetadata.setReferencedColumnName(
                !anColumn.referencedColumnName().isEmpty()?anColumn.referencedColumnName():idFieldName);
        // Найдем геттер
        String methodName = "get"
                + columnMetadata.getField().getName().substring(0, 1).toUpperCase()
                + columnMetadata.getField().getName().substring(1);
        if (methodMap.get(methodName) != null) {
            columnMetadata.setMethodGet(methodMap.get(methodName));
        } else { // попробуем найти is
            methodName = "is"
                    + columnMetadata.getField().getName().substring(0, 1).toUpperCase()
                    + columnMetadata.getField().getName().substring(1);
            if (methodMap.get(methodName) != null) {
                columnMetadata.setMethodGet(methodMap.get(methodName));
            }
        }
        // Найдем сеттер
        methodName = "set"
                + columnMetadata.getField().getName().substring(0, 1).toUpperCase()
                + columnMetadata.getField().getName().substring(1);
        if (methodMap.get(methodName) != null) {
            columnMetadata.setMethodSet(methodMap.get(methodName));
        }
        joinColumnMetadataList.add(columnMetadata);
        return columnMetadata;

    }

    private ColumnMetadata addColumnToMetaData(Map<String, Method> methodMap, Field field) {
        ColumnMetadata columnMetadata = new ColumnMetadata();
        Column anColumn = field.getAnnotation(Column.class);
        columnMetadata.setColumnName(anColumn.name());
        columnMetadata.setNullable(anColumn.nullable());
        columnMetadata.setField(field);
        columnMetadata
                .setColumnDefinition(anColumn.columnDefinition());
        columnMetadata.setIdFlag(field.isAnnotationPresent(Id.class));
        // Найдем геттер
        String methodName = "get"
                + columnMetadata.getField().getName().substring(0, 1).toUpperCase()
                + columnMetadata.getField().getName().substring(1);
        ;
        if (methodMap.get(methodName) != null) {
            columnMetadata.setMethodGet(methodMap.get(methodName));
        } else { // попробуем найти is
            methodName = "is"
                    + columnMetadata.getField().getName().substring(0, 1).toUpperCase()
                    + columnMetadata.getField().getName().substring(1);
            ;
            if (methodMap.get(methodName) != null) {
                columnMetadata.setMethodGet(methodMap.get(methodName));
            }
        }
        // Найдем сеттер
        methodName = "set"
                + columnMetadata.getField().getName().substring(0, 1).toUpperCase()
                + columnMetadata.getField().getName().substring(1);
        if (methodMap.get(methodName) != null) {
            columnMetadata.setMethodSet(methodMap.get(methodName));
        }
        columnMetadataList.add(columnMetadata);
        return columnMetadata;
    }

    /**
     * Возвращает описание таблицы из коллекции tableMetadataMap. <p> Если описания в коллекции
     * отсутствует - предварительно добавляет его туда. <p> Наличие проверяет по имени таблицы
     * <p>Если имя таблицы пустое - должен быть указан класс модели
     *
     * @param tableMetadataMap Коллекеция метаданных, где искать и куда добавлять
     * @param cls              Класс модели
     * @return TableMetadata - описание таблицы
     */
    public static TableMetadata getTableMetadataFromMap(
            Map<String, TableMetadata> tableMetadataMap,
            Class cls
    ) {
        TableMetadata tableMetadata = new TableMetadata(); // Создаем
        tableMetadata.loadTableMetadata(cls); // Получим все метаданные
        tableMetadataMap.put( // Загрузим в воллекцию
                tableMetadata.getTableName(),
                tableMetadata
        );
        return tableMetadata;
    }

    /**
     * Сканирует все аннотации @Table  и заполняет {@link biz.gelicon.core.repository.TableRepository#tableMetadataMap}
     *
     * @param prefix префикс пакетов для сканирования, например, "biz.gelicon.core.model"
     */
    public static void readTableMetadataAnnotation(
            String prefix
    ) {
        if (prefix == null) {prefix = "biz.gelicon.core";}
        logger.info("Reading @Table annotation...");
        Reflections reflections = new Reflections(prefix); // Рефлектор
        // Считываем все аннотации Table
        Set<Class<?>> entyties = reflections.getTypesAnnotatedWith(Table.class);
        entyties.forEach(e -> {
            String tblName = e.getAnnotation(Table.class).name();
            NoRegister noRegisterFlag = e.getAnnotation(NoRegister.class);
            if(noRegisterFlag==null) {
                logger.info(e.getName()+": "+tblName);
                getTableMetadataFromMap(TableRepository.tableMetadataMap,e);
            } else {
                logger.info(e.getName()+": "+tblName);
                logger.info("skip (found @NoRegister annotation)");
            }
        });
        logger.info("@Table annotation readed");
    }

}
