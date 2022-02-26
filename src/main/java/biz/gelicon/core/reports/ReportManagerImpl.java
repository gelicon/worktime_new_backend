package biz.gelicon.core.reports;

import biz.gelicon.services.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@Component
public class ReportManagerImpl implements ReportManager {
    private static final Logger logger = LoggerFactory.getLogger(ReportManagerImpl.class);
    private List<ReportDescriptionImpl> reports = new ArrayList<>();
    private ReportService currService;
    private boolean overlappingReportFlag = true;

    @Autowired
    private DataSource dataSource;

    // загрузка всех связанных модулей-печатных форм
    public void loadReports() {
        logger.info("Load reports...");
        List<ReportService> sevices = ReportService.getInstances();
        sevices.forEach(s-> {
            currService = s;
            s.registerReport(this);
        });
        logger.info("Load reports [Ok]");
    }

    public List<ReportDescriptionImpl> getReports() {
        return Collections.unmodifiableList(reports);
    }

    @Override
    public ReportDescription registerReport(String code, String name) {
        if(code==null || code.isEmpty())
            throw new RuntimeException("Code report is empty");
        if(name==null || name.isEmpty())
            throw new RuntimeException("Name report is empty");
        logger.info("\tRegister report {}:{}",code,name);
        ReportDescriptionImpl report = new ReportDescriptionImpl(code, name);
        report.setHolder(currService);
        // override печатных форм
        ReportDescriptionImpl regRep = findByCode(code);
        if(regRep!=null) {
            // если запрет перекрытия отчетов то
            if(!overlappingReportFlag) {
                logger.warn("-- Duplicate code report  {}:{}",regRep.getCode(),regRep.getName());
                // не регистрируем, но объект возвращаем, чтобы ПФ не ломала загрузку приложения
                return report;
            }
            reports.remove(regRep);
            logger.warn("-- Was remove overlapping report {}:{}",regRep.getCode(),regRep.getName());
        }
        reports.add(report);
        return report;
    }

    private ReportDescriptionImpl findByCode(String code) {
        return reports.stream()
                .filter(r->r.getCode().toLowerCase().equals(code.toLowerCase()))
                .findFirst()
                .orElse(null);
    }

    @Override
    public ReportParamOptions createOptionCapCode(Integer capCodeTypeId, boolean nulleable, boolean cashable) {
        return new OptionCapCodeImpl(capCodeTypeId,nulleable,cashable);
    }

    @Override
    public ReportParamOptions createOptionCapCodeMultiple(Integer capCodeTypeId, boolean nulleable, boolean cashable) {
        return new OptionCapCodeImpl(capCodeTypeId,nulleable,cashable,true);
    }

    @Override
    public ReportParamOptions createOptionDataLookup(String uri, String dataForPost, boolean nulleable) {
        return new OptionDataLookup(uri,dataForPost,nulleable);
    }

    @Override
    public ReportParamOptions createOptionSelect(String uri, String dataForPost, String valueName, String displayValueName, boolean nulleable, boolean cashable) {
        return new OptionSelect(uri,dataForPost, valueName, displayValueName,nulleable,cashable);
    }

    @Override
    public OptionSelect createOptionSelect(List<OptionForSelectParam> values, boolean nulleable) {
        return new OptionSelect(values,nulleable);
    }

    @Override
    public ReportParamOptions createOption(boolean nulleable) {
        return new OptionParam(nulleable);
    }

    @Override
    public String runReport(String code, Map<String, Object> params) {
        ReportDescriptionImpl report = findByCode(code);
        if(report==null)
            throw new RuntimeException(String.format("Report %s not found",code));
        return report.getHolder().runReport(code,params);
    }

    public boolean isOverlappingReportFlag() {
        return overlappingReportFlag;
    }

    public void setOverlappingReportFlag(boolean overlappingReportFlag) {
        this.overlappingReportFlag = overlappingReportFlag;
    }

    public void testJNDI(){
        logger.info("Test JNDI ...");
        try {
            InitialContext ctx = new InitialContext();
            DataSource datasource = (DataSource) ctx.lookup("java:comp/env/jdbc/gelicon-core-datasource");
            logger.info("lookup JNDI datasource. Result -> {}",datasource);
        } catch (NamingException ex) {
            throw new RuntimeException(ex);
        }
        logger.info("Test JNDI [ok]");
    }
}
