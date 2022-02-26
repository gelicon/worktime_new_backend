package biz.gelicon.core.dialect;

public abstract class BaseDialect implements DBDialect{
    @Override
    public String countRecord(String sql) {
        return "SELECT COUNT(*) FROM (" + sql + ") AS T";
    }
}
