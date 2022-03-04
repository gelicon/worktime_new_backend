package biz.gelicon.core.response.exceptions;

public class RepositoryNotSettingException extends RuntimeException {
    public RepositoryNotSettingException() {
        super("Не установлен репозиторий в class TablenameService - ошибка программирования");
    }
}
