package biz.gelicon.core.artifacts;

/**
 * Типы артефактов
 */
public enum ArtifactKinds {

/**
 * 1	01	Печатные формы
 * 2	02	Библиотеки функций
 * 3	03	Модули перегрузки
 * 4	04	Функции пользователя
 * 5	05	Расчеты для отчетности
 * 6	06	Расчеты заработной платы
 * 7	07	Контейнер данных
 * 8	08	Расчеты документов
 * 9	09	Типовые бухгалтерские операции
 * 10	10	Библиотеки диалогов
 * 11	11	Нумератор
 * 12	12	Признак
 * 13	13	Константа
 * 15	15	Справочник
 * 16	16	Контур
 * 17	17	Таблица пользователя
 * 18	18	Статические документы (HTML,текст,...)
 * 19	19	Статические бинарные ресурсы (GIF,JPG,...)
 * 20	20	Динамические документы
 * 21	21	Динамические бинарные ресурсы
 * 22	22	XML ресурсы
 * 23	23	Обработчики форм
 * 24	24	JAVA класс
 * 27	27	Модель данных
 * 28	28	Инструкции пользователя
  */
    REPORT,
    LIBRARY,
    OVER_UNIT,
    USER_FUNC,
    CALC_REPORT,
    CALC_PAYROLL,
    DATA_BOX,
    CALC_DOCUMENT,
    ACCOUNT_OPER,
    DIALOGS,
    NUMERATOR,
    ATTRIBUTE,
    CONST,
    REF_BOOK,
    CONTOUR,

    NOT_USED_17,
    NOT_USED_18,
    NOT_USED_19,
    NOT_USED_20,
    NOT_USED_21,
    NOT_USED_22,
    NOT_USED_23,
    NOT_USED_24,
    NOT_USED_25,
    NOT_USED_26,
    NOT_USED_27,
    NOT_USED_28;

    /**
     * Возвращает значение capresourcetype_id для нумератора. Оно отличается в большую сторону на 1
     * @return значение
     */
    public int getResourceType() {
        return this.ordinal()+1;
    }

    /**
     * Возвращает список capresourcetype_id для всех используемых нумераторов.
     * @return список
     */
    public static int[] getExternalResourceType() {
        return new int[]{NUMERATOR.getResourceType(),REPORT.getResourceType()};
    }
}
