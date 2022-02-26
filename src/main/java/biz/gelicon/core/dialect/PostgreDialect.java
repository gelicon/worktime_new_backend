package biz.gelicon.core.dialect;

public class PostgreDialect extends BaseDialect implements DBDialect{

    private final String schemaName;

    public PostgreDialect(String schemaName) {
        this.schemaName = schemaName;
    }

    @Override
    public String sequenceNextValueSQL(String seqName) {
        return String.format("SELECT nextval('%s')", seqName);
    }

    @Override
    public String alterSequence(String seqName, Integer value) {
        return  "ALTER SEQUENCE " + seqName + " RESTART WITH " + value;
    }

    @Override
    public String createSequence(String sequenceName, Integer startValue) {
        return "CREATE SEQUENCE "+sequenceName+" INCREMENT BY 1 " +
                " MINVALUE 1  MAXVALUE 2147483647 START "+startValue+" CACHE 1 NO CYCLE";
    }

    @Override
    public String sequenceExist(String sequenceName) {
        return "SELECT COUNT(*) \n"
                + "FROM   information_schema.sequences \n"
                + "WHERE  sequence_name = '" + sequenceName + "' \n"
                + "  AND  sequence_schema = '" + schemaName + "'";
    }

    @Override
    public String tableExist(String tableName) {
        return ""
                + "SELECT COUNT(*) \n"
                + "FROM   information_schema.tables \n"
                + "WHERE  table_name = '" + tableName + "' \n"
                + "  AND  table_schema = '" + schemaName + "'";
    }

    @Override
    public String viewExist(String viewName) {
        return ""
                + "SELECT COUNT(*) \n"
                + "FROM   information_schema.views \n"
                + "WHERE  table_name = '" + viewName + "' \n"
                + "  AND  table_schema = '" + schemaName + "'";
    }

    @Override
    public boolean isDulpicateKeyConstraint(String message) {
        return false;
    }

    @Override
    public String extractColumnNameFromDuplicateMessage(String message) {
        final String keyPhrase = "Ключ \"(";
        int idx = message.indexOf(keyPhrase);
        if(idx>=0) {
            String stmp = message.substring(idx+keyPhrase.length());
            int end = stmp.indexOf(")");
            if(end<0) return null;
            return stmp.substring(0,end);
        }
        return null;
    }

    @Override
    public String extractTableNameFromForeignKeyMessage(String message) {
        String keyPhrase = "всё ещё есть ссылки в таблице \"";
        int idx = message.indexOf(keyPhrase);
        if(idx<0) {
            // еще один вариант сообщения
            keyPhrase = "отсутствует в таблице \"";
            idx = message.indexOf(keyPhrase);
        }
        if(idx>=0) {
            String stmp = message.substring(idx+keyPhrase.length());
            int end = stmp.indexOf("\"");
            if(end<0) return null;
            return stmp.substring(0,end);
        }
        return null;
    }

    @Override
    public String stringAggFunc(String columnName) {
        return String.format("string_agg(%s,',')",columnName);
    }

    /**
     * Модифицирует SQL запрос для проверки существования записей в результате
     * @param sql
     * @return
     */
    @Override
    public String existsSql(String sql) {
        return "SELECT 1 FROM (" + sql + ") T LIMIT 1";
        // Для MSSQL так
        // return "SELECT TOP(1) 1 FROM (" + sql + ") AS T";
    }

    @Override
    public String limitAndOffset(Integer limit, Integer offset) {
        if(limit==null && offset==null) {
            return "";
        }
        return (limit!=null?"LIMIT "+limit+" ":"")+(offset!=null?"OFFSET "+offset:"");
    }

}
