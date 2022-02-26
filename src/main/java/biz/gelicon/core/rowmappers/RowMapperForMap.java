package biz.gelicon.core.rowmappers;

import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RowMapperForMap implements RowMapper<Map<String,Object>> {

    private ResultSetMetaData resultSetMetaData;
    private List<String> columnNames = new ArrayList<>();

    private void prepareDataSet(ResultSet resultSet) throws SQLException {
        if(resultSetMetaData==null) {
            resultSetMetaData = resultSet.getMetaData();
            int colCount = resultSetMetaData.getColumnCount();
            for (int idx = 1; idx <= colCount; idx++) {
                columnNames.add(resultSetMetaData.getColumnLabel(idx).toLowerCase());
            }
        }
    }

    @Override
    public Map<String, Object> mapRow(ResultSet resultSet, int numRow) throws SQLException {
        prepareDataSet(resultSet);
        Map<String, Object> row = new HashMap<>();
        int colCount = resultSetMetaData.getColumnCount();
        for (int idx = 1; idx <= colCount; idx++) {
            Object value = resultSet.getObject(idx);
            row.put(columnNames.get(idx-1),value);
        }
        return row;
    };

}
