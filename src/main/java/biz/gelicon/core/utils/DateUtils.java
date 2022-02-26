package biz.gelicon.core.utils;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.temporal.IsoFields;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import static java.time.temporal.ChronoField.DAY_OF_MONTH;
import static java.time.temporal.TemporalAdjusters.*;

public class DateUtils {

    private static final DateFormatThreadSafe dateFormat = new DateFormatThreadSafe("dd.MM.yyyy");
    private final static DateFormatThreadSafe datetimeFormat = new DateFormatThreadSafe("dd.MM.yyyy HH:mm:ss");
    private final static DateFormatThreadSafe jsonDatetimeFormat = new DateFormatThreadSafe("yyyy-MM-dd'T'HH:mm:ss.SSSX");

    /**
     * Дату в строку формата "31.11.2020"
     * +kav: потокобезопасный
      */
    public static String dateToStr(Date d) {
        return d == null ? null : dateFormat.get().format(d);
    }

    /**
     * Дату-время в строку формата "31.11.2020 22:48:37"
     * +kav: потокобезопасный
     */
    public static String datetimeToStr(Date d) {
        return d == null ? null : datetimeFormat.get().format(d);
    }

    /**
     * округляет дату-время до даты
     * @param d - дата-вермя
     * @return - дата-вермя, округленная до дня
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
     * Возвращает дату сейчасовую
     * @return дата-время сегодня, округленная до дня
     */
    public static Date getDate() {
        return datetimeToDate(new Date());
    }


    /**
     * Преобразует строку-дату из JSON в дату
     *
     * @param dateString
     * @return дату или null в случае неудачи
     */
    public static Date json2date(String dateString) {
        try {
            return jsonDatetimeFormat.get().parse(dateString);
        } catch (ParseException ex) {
            throw new RuntimeException(ex);
        }
    }

    public static Date stringToDate(String dateString) {
        try {
            return dateFormat.get().parse(dateString);
        } catch (ParseException ex) {
            throw new RuntimeException(ex);
        }
    }

    public static Date stringToDateTime(String dateString) {
        try {
            return datetimeFormat.get().parse(dateString);
        } catch (ParseException ex) {
            throw new RuntimeException(ex);
        }
    }

    public static Date getMaxDate() {
        try {
            return dateFormat.get().parse("01.01.2099");
        } catch (ParseException ex) {
            throw new RuntimeException(ex);
        }
    }

    ;

    public static Date getMinDate() {
        try {
            return dateFormat.get().parse("01.01.1900");
        } catch (ParseException ex) {
            throw new RuntimeException(ex);
        }
    }

    ;

    public static Date atStartOfDay(Date date) {
        LocalDateTime localDateTime = dateToLocalDateTime(date);
        LocalDateTime startOfDay = localDateTime.with(LocalTime.MIN);
        return localDateTimeToDate(startOfDay);
    }

    public static Date atEndOfDay(Date date) {
        LocalDateTime localDateTime = dateToLocalDateTime(date);
        LocalDateTime endOfDay = localDateTime.with(LocalTime.MAX);
        return localDateTimeToDate(endOfDay);
    }

    public static Date atStartOfYear(Date date) {
        LocalDateTime localDateTime = dateToLocalDateTime(date);
        LocalDateTime startOfDay = localDateTime
                .with(firstDayOfYear())
                .with(LocalTime.MIN);
        return localDateTimeToDate(startOfDay);
    }

    public static Date atEndOfYear(Date date) {
        LocalDateTime localDateTime = dateToLocalDateTime(date);
        LocalDateTime endOfDay = localDateTime
                .with(lastDayOfYear())
                .with(LocalTime.MAX);
        return localDateTimeToDate(endOfDay);
    }

    private static LocalDateTime dateToLocalDateTime(Date date) {
        return LocalDateTime.ofInstant(date.toInstant(), ZoneId.systemDefault());
    }

    private static Date localDateTimeToDate(LocalDateTime localDateTime) {
        return Date.from(localDateTime.atZone(ZoneId.systemDefault()).toInstant());
    }

    public static Date atStartOfQuarter(Date date) {
        LocalDateTime localDateTime = dateToLocalDateTime(date);
        LocalDateTime startOfDay = localDateTime
                .with(localDateTime.getMonth().firstMonthOfQuarter())
                .with(firstDayOfMonth())
                .with(LocalTime.MIN);
        return localDateTimeToDate(startOfDay);
    }

    public static Date atEndOfQuarter(Date date) {
        LocalDateTime localDateTime = dateToLocalDateTime(date);
        LocalDateTime endOfDay = localDateTime
                .with(localDateTime.getMonth().firstMonthOfQuarter().plus(2))
                .with(lastDayOfMonth())
                .with(LocalTime.MAX);
        return localDateTimeToDate(endOfDay);
    }

    public static Date atStartOfMonth(Date date) {
        LocalDateTime localDateTime = dateToLocalDateTime(date);
        LocalDateTime startOfDay = localDateTime
                .with(firstDayOfMonth())
                .with(LocalTime.MIN);
        return localDateTimeToDate(startOfDay);
    }

    public static Date atEndOfMonth(Date date) {
        LocalDateTime localDateTime = dateToLocalDateTime(date);
        LocalDateTime endOfDay = localDateTime
                .with(lastDayOfMonth())
                .with(LocalTime.MAX);
        return localDateTimeToDate(endOfDay);
    }

    static class DateFormatThreadSafe extends ThreadLocal<DateFormat> {
        private final String pattern;

        public DateFormatThreadSafe(String pattern) {
            this.pattern = pattern;
        }

        @Override
        protected DateFormat initialValue() {
            return new SimpleDateFormat(this.pattern);
        }

    };

}
