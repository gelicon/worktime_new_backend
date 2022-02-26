package biz.gelicon.core.dialect;

import biz.gelicon.core.response.exceptions.PostRecordException;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class FireBirdDialect  extends BaseDialect implements DBDialect{

    public FireBirdDialect() {
    }

    @Override
    public String sequenceNextValueSQL(String seqName) {
        return String.format("SELECT NEXT VALUE FOR %s FROM DUAL", seqName);
    }

    @Override
    public String alterSequence(String sequenceName, Integer value) {
        return String.format("ALTER SEQUENCE %s RESTART WITH %d", sequenceName,value.intValue());
    }

    @Override
    public String createSequence(String sequenceName, Integer startValue) {
        return "CREATE SEQUENCE "+sequenceName;
    }

    @Override
    public String sequenceExist(String sequenceName) {
        return String.format("SELECT COUNT(*) " +
                "FROM RDB$GENERATORS " +
                "WHERE upper(RDB$GENERATOR_NAME) = '%s'", sequenceName.toUpperCase());
    }

    @Override
    public String tableExist(String tableName) {
        return String.format("SELECT COUNT(*) " +
                "FROM RDB$RELATIONS " +
                "WHERE upper(RDB$RELATION_NAME) = '%s'",tableName.toUpperCase());
    }

    @Override
    public String viewExist(String viewName) {
        return String.format("SELECT COUNT(*) " +
                "FROM RDB$VIEW_RELATIONS " +
                "WHERE upper(RDB$VIEW_NAME)='%s'",viewName.toUpperCase());
    }

    @Override
    public boolean isDulpicateKeyConstraint(String message) {
        return message.startsWith("violation of PRIMARY or UNIQUE KEY constraint");
    }

    @Override
    public String extractColumnNameFromDuplicateMessage(String message) {
        final String keyPhrase = "Problematic key value is (";
        int idx = message.indexOf(keyPhrase);
        if(idx>=0) {
            String stmp = message.substring(idx+keyPhrase.length());

            Matcher m = Pattern.compile("(\\\"[\\w\\s]+\\\")").matcher(stmp);
            final List<String> matches = new ArrayList<>();
            while (m.find()) {
                matches.add(m.group(0).replace("\"", ""));
            }
            return matches.isEmpty()?null:String.join(",",matches);
        }
        return null;
    }

    @Override
    public String extractTableNameFromForeignKeyMessage(String message) {
        int idx;
        String keyPhrase;
        // при удалении
        boolean forDelete = message.indexOf("Foreign key references are present for the record; Problematic key value") >= 0;
        if(forDelete) {
            keyPhrase = " on table \"";
            idx = message.indexOf(keyPhrase);
        } else {
            // при вставке или обновлении
            keyPhrase = "Problematic key value is (\"";
            idx = message.indexOf(keyPhrase);
        }
        if(idx>=0) {
            String stmp = message.substring(idx+keyPhrase.length());
            int end = stmp.indexOf("\"");
            if(end<0) return null;
            // при вставке или обновлении тмеем дело с FK
            // Оно заканчивается _ID
            if(!forDelete) {
                end-=3;
            }
            return stmp.substring(0,end);
        };
        return null;

    }

    @Override
    public String stringAggFunc(String columnName) {
        return String.format("LIST(%s,',')",columnName);
    }

        @Override
    public String existsSql(String sql) {
        return "SELECT FIRST 1 1 FROM (" + sql + ")";
    }

    @Override
    public String limitAndOffset(Integer limit, Integer offset) {
        if(limit==null && offset==null) {
            return "";
        }
        return (offset!=null?"OFFSET "+offset+" ROWS ":"")+(limit!=null?"FETCH FIRST "+limit+" ROWS ONLY ":"");
    }

}
