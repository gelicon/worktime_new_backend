package biz.gelicon.core.utils;


import biz.gelicon.core.dialect.DialectFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.jdbc.core.JdbcTemplate;

import java.sql.SQLException;

/**
 * Различные методы работы с базой данных
 */
public class DatabaseUtils {
    static Logger logger = LoggerFactory.getLogger(DatabaseUtils.class);

    /**
     * Формирует текст ошибки из исключения в читаемом виде todo Не доделана
     *
     * @param e Исключение
     * @return читаемый текст
     */
    public static String makeErrorMessage(Exception e) {
        if (e == null) {
            return "Ошибка";
        }
        String s = e.getMessage() + " - дополнить анализ в makeErrorMessage";
        if (e instanceof DuplicateKeyException) {
            s = "Нарушение уникальности";
        } else if (e instanceof DataIntegrityViolationException) {
            s = "Нарушение ссылочной целостности. Возможно, на запись есть ссылки.";
        }
        return s;
    }


    /**
     * Сделающее значение последовательности
     * @param objName
     * @return
     */
    public static int sequenceNextValue(String objName) {
        JdbcTemplate jdbcTemplate = OrmUtils.getJdbcTemplate();
        String sql = DialectFactory.getDialect().sequenceNextValueSQL(objName);
        return jdbcTemplate.queryForObject(sql,Integer.class);
    }

    /**
     * Установка последовательности в значение
     * @param sequenceName
     * @param startValue
     */
    public static void setSequence(String sequenceName, Integer startValue) {
        JdbcTemplate jdbcTemplate = OrmUtils.getJdbcTemplate();
        String sql = DialectFactory.getDialect().alterSequence(sequenceName,startValue);
        jdbcTemplate.update(sql);
    }

    /**
     * Проверка существования последовательности
     * @param sequenceName - имя последовательности
     * @return
     */
    public static boolean checkSequenceExist(String sequenceName) {
        JdbcTemplate jdbcTemplate = OrmUtils.getJdbcTemplate();
        String sqlText = DialectFactory.getDialect().sequenceExist(sequenceName);
        return jdbcTemplate.queryForObject(sqlText, Integer.class) == 1;
    }

    /**
     * Выполнение SQL оператора
     *
     * @param sqlText
     * @param jdbcTemplate
     */
    public static void executeSql(String sqlText, JdbcTemplate jdbcTemplate) {
        jdbcTemplate.update(sqlText);
    }

    /**
     * Проверка существования таблицы
     * @param tableName - имя таблицы
     * @return
     */
    public static boolean checkTableExist(String tableName) {
        JdbcTemplate jdbcTemplate = OrmUtils.getJdbcTemplate();
        String sqlText = DialectFactory.getDialect().tableExist(tableName);
        return jdbcTemplate.queryForObject(sqlText, Integer.class) != 0;
    }

    /**
     * Проверка существования представления
     * @param viewName - имя представления
     * @return
     */
    public static boolean checkViewExist(String viewName) {
        JdbcTemplate jdbcTemplate = OrmUtils.getJdbcTemplate();
        String sqlText = DialectFactory.getDialect().viewExist(viewName);
        return jdbcTemplate.queryForObject(sqlText, Integer.class) != 0;
    }

}
