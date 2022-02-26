package biz.gelicon.core.security;

public enum Permission {
    NONE,
    SELECT,
    INSERT,
    UPDATE,
    DELETE,
    SEARCH,
    DEBUG,
    ABORT,
    CHOWNER,
    EXECUTE,
    ADMIN;
    static final String[] permNames =
            new String[]{"-","Выбор","Добавить", "Изменить","Удалить",
                         "Найти","Отладить","Прервать","Сменить владельца",
                         "Выполнить"};
    public String getNameRu() {
        return permNames[ordinal()];
    }
}
