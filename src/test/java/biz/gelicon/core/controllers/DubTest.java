package biz.gelicon.core.controllers;

import biz.gelicon.core.reports.ReportManagerImpl;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.HashMap;
import java.util.Map;

/**
 * Тесты Дубинского во время разработки Должна быть установлена переменная среды Windows tel в
 * значение +79221554762
 */

@Disabled // НИ ПРИ КАКИХ ОБСТОЯТЕЛЬСТВАХ НЕ УДАЛЯТЬ!!!
@SpringBootTest(properties = {
        "gelicon.orm.recreatedatabase=false",
        "spring.datasource.url=jdbc:postgresql://10.15.3.39:5432/GC_DEVELOP_TRUNK?currentSchema=dbo",
        "gelicon.report.restcrictOverlapping=false"
})
public class DubTest  {
    static Logger logger = LoggerFactory.getLogger(DubTest.class);

    @Autowired
    ReportManagerImpl reportManager;

    @Test
    public void testDub() throws Exception {
        if (!"+79221554762".equals(System.getenv("tel"))) {return;}
        System.out.println("dubTest...");
        System.out.println("dubTest...Ok");
    }

    @Test
    public void runReportUSR_LST() throws Exception {
        if (!"+79221554762".equals(System.getenv("tel"))) {return;}
        String repCode = "USR-LST";
        Map<String,Object> params = new HashMap<>();
        params.put("status",1301); // Активные
        params.put("type",1); // Администраторы
        String url = reportManager.runReport(repCode, params);
        logger.info("url={}",url);
    }

    @Test
    public void runReportUSR_DTL() throws Exception {
        if (!"+79221554762".equals(System.getenv("tel"))) {return;}
        String repCode = "USR-DTL";
        Map<String,Object> params = new HashMap<>();
        params.put("id",1); // SYSDBA
        params.put("print_progusergroup",1); // Печатать группы
        params.put("print_accessrole",true); // Печатать доступа к ролям
        String url = reportManager.runReport(repCode, params);
        logger.info("url={}", url);
    }

    @Test
    public void DocumentRealTransitTest() {
        logger.info("!!!!!!!!!!!!!!!                   !!!!!!!!!!!!!!!!!!!!!!!");
    }


}
