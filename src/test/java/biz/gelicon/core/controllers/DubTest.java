package biz.gelicon.core.controllers;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * Тесты Дубинского во время разработки
 * Должна быть установлена переменная среды Windows USERNAME в dav
 */

@Disabled // НИ ПРИ КАКИХ ОБСТОЯТЕЛЬСТВАХ НЕ УДАЛЯТЬ!!!
@SpringBootTest(properties = {
        "gelicon.orm.recreatedatabase=false",
        "jdbc:postgresql://localhost:5432/worktime_dev?currentSchema=test"
})
public class DubTest  {
    static Logger logger = LoggerFactory.getLogger(DubTest.class);
    private static final String userName = "dav";

    @Test
    public void testDub() throws Exception {
        if (noCheck()) return;
        System.out.println("dubTest...");
        System.out.println("dubTest...Ok");
    }

    @Test
    public void DocumentRealTransitTest() {
        if (noCheck()) return;
        logger.info("!!!!!!!!!!!!!!!                   !!!!!!!!!!!!!!!!!!!!!!!");
    }

    /**
     * Проверка на пользователя dav
     * @return true - не проверять
     */
    public boolean noCheck() {
        String username = System.getenv("USERNAME");
        return !userName.equals(username);
    }


}
