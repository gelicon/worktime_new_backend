package biz.gelicon.core.dialect;

/**
 * Различные системные функции, зависящие от СУБД
 */
public interface DBDialect {

    public String sequenceNextValueSQL(String seqName);

    public String alterSequence(String sequenceName, Integer value);

    public String createSequence(String sequenceName, Integer startValue);

    /**
     * Возвращает текст запроса для проверки существования последовательности
     *
     * @param sequenceName  - имя последовательности
     * @return
     */
    public String sequenceExist(String sequenceName);
    /**
     * Возвращает текст запроса для проверки существования таблицы
     *
     * @param tableName  - имя таблицы
     * @return
     */
    public String tableExist(String tableName);
    /**
     * Возвращает текст запроса для проверки существования представления
     *
     * @param viewName  - имя представления
     * @return
     */
    public String viewExist(String viewName);

    /**
     * Уточнение DataIntegrityViolationException. Дело в  том, что в DataIntegrityViolation
     * для некоторых серверов попадаются ошибки нарушения первичного ключа
     * Например, для Firebird
     * 
     * @param message
     * @return
     */
    public boolean isDulpicateKeyConstraint(String message);

    public String extractColumnNameFromDuplicateMessage(String message);

    public String extractTableNameFromForeignKeyMessage(String message);

    public String stringAggFunc(String columnName);

    /**
     * Преобразует текст запроса для выполнения запроса на возвращение одной или нуля записей
     *
     * @param sql
     * @return
     */
    public String existsSql(String sql);

    /**
     * Преобразует текст запроса для возвращений количества записей в запросе
     *
     * @param sql
     * @return
     */
    public String countRecord(String sql);


    public String limitAndOffset(Integer limit, Integer offset);
}
