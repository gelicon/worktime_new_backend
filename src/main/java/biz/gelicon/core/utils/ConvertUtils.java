package biz.gelicon.core.utils;

import biz.gelicon.core.artifacts.ArtifactKinds;
import biz.gelicon.core.response.DataResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 * Общие методы конвертирования данных
 */
public class ConvertUtils {

    private static final Logger logger = LoggerFactory.getLogger(ConvertUtils.class);
    private final static SimpleDateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy");
    private final static SimpleDateFormat datetimeFormat = new SimpleDateFormat(
            "dd.MM.yyyy HH:mm:ss");

    /**
     * Возвращает true для пустой или пробельной строки и null
     *
     * @param s строка
     * @return результат
     */
    public static boolean empty(String s) {
        return s == null || s.trim().isEmpty();
    }

    /**
     * Конвертирует дату в строку формата "31.11.2020"
     *
     * @param d дата
     * @return результат
     */
    public static String dateToStr(Date d) {
        return d == null ? null : dateFormat.format(d);
    }

    /**
     * Конвертирует строку формата "31.11.2020" в дату
     *
     * @param s строка
     * @return результат
     */
    public static Date strToDate(String s) {
        if (empty(s)) {return null;}
        try {
            return dateFormat.parse(s);
        } catch (ParseException e) {
            String errText = String.format("Converting %s to date filed", s);
            throw new RuntimeException(errText, e);
        }
    }

    /**
     * округляет дату-время до даты
     *
     * @param d датавремя
     * @return результат
     */
    public static Date datetimeToDate(Date d) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(d);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        return calendar.getTime();
    }

    /**
     * Возвращает минимальную дату, равную 01.01.1900
     *
     * @return
     */
    public static Date getMinDate() {
        try {
            return dateFormat.parse("01.01.1900");
        } catch (ParseException e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    /**
     * Возвращает максимальную дату, равную 31.12.2099
     *
     * @return
     */
    public static Date getMaxDate() {
        try {
            return dateFormat.parse("31.12.2099");
        } catch (ParseException e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    /**
     * Преобразует итератор в поток
     *
     * @param iterator
     * @param <T>
     * @return
     */
    public static <T> Stream<T> getStreamFromIterator(Iterator<T> iterator) {
        Spliterator<T> spliterator = Spliterators.spliteratorUnknownSize(iterator, 0);
        return StreamSupport.stream(spliterator, false);
    }

    /**
     * Преобразует массив целых в струку, пригодную для подстановки в секцию IN SQL запроса со
     * скобками например, (1, 3, 5, 7)
     *
     * @param array
     * @return
     */
    public static String arrayToSQLString(int[] array) {
        return Arrays.stream(array)
                .mapToObj(String::valueOf)
                .collect(Collectors.joining(",", "(", ")"));
    }

}
