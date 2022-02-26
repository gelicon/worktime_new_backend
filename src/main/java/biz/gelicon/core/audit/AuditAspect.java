package biz.gelicon.core.audit;

import biz.gelicon.core.annotations.Audit;
import biz.gelicon.core.annotations.RestrictStoreToAudit;
import biz.gelicon.core.jobs.JobDispatcher;
import biz.gelicon.core.model.Proguser;
import biz.gelicon.core.security.UserDetailsImpl;
import biz.gelicon.core.utils.OrmUtils;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.Arrays;
import java.util.EnumSet;
import static biz.gelicon.core.audit.AuditKind.*;

@Component
@Aspect
public class AuditAspect {
    private static final Logger logger = LoggerFactory.getLogger(AuditAspect.class);
    private static final EnumSet<AuditKind> KINDS_FOR_ENTITTY_OPER =
            EnumSet.of(CALL_FOR_EDIT, CALL_FOR_ADD, CALL_FOR_SAVE_INSERT,CALL_FOR_SAVE_UPDATE, CALL_FOR_DELETE);
    private static final EnumSet<AuditKind> KINDS_FOR_EDIT = EnumSet.of(CALL_FOR_EDIT, CALL_FOR_ADD);
    private static final EnumSet<AuditKind> KINDS_FOR_SAVE = EnumSet.of(CALL_FOR_SAVE_INSERT,CALL_FOR_SAVE_UPDATE);
    private static final String NOSTORE = "Данные не сохранены из-за ограничений конфиденциальности";

    @Autowired
    private AuditProcessor processor;

    @Pointcut("@annotation(org.springframework.web.bind.annotation.RequestMapping)")
    public void auditRequestMapping() {}

    @Pointcut("@annotation(biz.gelicon.core.annotations.Audit)")
    public void auditMethodPermission() {}

    @Around("auditRequestMapping() && auditMethodPermission()")
    public Object aroundAudit(ProceedingJoinPoint joinPoint) throws Throwable {
        AuditRecord rec = buildAuditRecord(joinPoint);
        Object result=null;
        long start = System.currentTimeMillis();
        try {
            result = joinPoint.proceed();
        } catch (Throwable ex) {
            rec.setFaultInfo(ex.getClass().toString()+": "+ex.getMessage()+
                            (ex.getCause()!=null?"\nCause "+ex.getCause().getClass().toString()+": "+ex.getCause().getMessage():""));
            rec.setDuration(System.currentTimeMillis()-start);
            processor.pushToProcess(rec);
            throw ex;
        } finally {
            if(result!=null) {
                setOutput(rec,joinPoint,result);
                rec.setDuration(System.currentTimeMillis()-start);
                processor.pushToProcess(rec);
            }
        }
        return result;
    }

    private void setOutput(AuditRecord rec, ProceedingJoinPoint joinPoint, Object result) {
        Method method = ((MethodSignature) joinPoint.getSignature()).getMethod();
        Audit auditAnn = method.getAnnotation(Audit.class);
        if(method.getAnnotation(RestrictStoreToAudit.class) == null) {
            if(auditAnn.storeOutput()) {
                rec.setOutputObject(result);
            }
        } else {
            rec.setOutputObject(NOSTORE);
        }
    }

    private AuditRecord buildAuditRecord(ProceedingJoinPoint joinPoint) {
        Method method = ((MethodSignature) joinPoint.getSignature()).getMethod();
        Audit auditAnn = method.getAnnotation(Audit.class);
        AuditKind[] kinds = auditAnn.kinds();
        Class<?> clsHolder = method.getDeclaringClass();
        RequestMapping rootRequestMapping = method.getDeclaringClass().getAnnotation(RequestMapping.class);
        String prefix = rootRequestMapping!=null?rootRequestMapping.value()[0]:"";


        boolean auth = !auditAnn.noAuthentication();
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Proguser pu = null;
        if(authentication!=null) {
            pu = ((UserDetailsImpl) authentication.getPrincipal()).getProgUser();
        }
        AuditRecord record = new AuditRecord();
        if(auth) {
            record.setProguser(new AuditRecord.ProguserInfo(pu));
        }

        record.setEntity(extractEntityName(method, kinds));
        record.setKind(clarificationKind(joinPoint.getArgs(),kinds, method).ordinal());
        // тут пишем полный путь
        record.setPath(normalizeObjectName(prefix,method.getAnnotation(RequestMapping.class).value()[0]));
        if(auditAnn.storeInput()) {
            record.setInputObject(extractInputObject(method,joinPoint.getArgs()));
        }
        return record;
    }

    private String normalizeObjectName(String pref, String url) {
        pref = pref.endsWith("/")?pref:pref+"/";
        url = url.startsWith("/")?url.substring(1):url;
        return pref+url;
    }

    private Object extractInputObject(Method method, Object[] args) {
        for (int i = 0; i < method.getParameters().length; i++) {
            Parameter p = method.getParameters()[i];
            if(p.getAnnotation(RequestBody.class) != null) {
                if(p.getAnnotation(RestrictStoreToAudit.class) == null) {
                    return args[i];
                } else {
                    return NOSTORE;
                }

            }
        }
        return null;
    }

    // путь вызова, в нем на первом месте сущность, если kinds in CALL_FOR_EDIT, CALL_FOR_ADD, CALL_FOR_SAVE_INSERT, CALL_FOR_SAVE_UPDATE,CALL_FOR_DELETE
    private String extractEntityName(Method method, AuditKind[] kinds) {
        String methodPath = method.getAnnotation(RequestMapping.class).value()[0];
        EnumSet<AuditKind> kindSet = EnumSet.noneOf(AuditKind.class);
        kindSet.addAll(Arrays.asList(kinds));
        kindSet.removeAll(KINDS_FOR_ENTITTY_OPER);
        // это вызов с entity
        if(kindSet.isEmpty()) {
            return methodPath.split("/")[0];
        }
        return null;
    }

    private AuditKind clarificationKind(Object[] args,AuditKind[] kinds,Method method) {
        if(kinds.length==1) {
            return kinds[0];
        };
        EnumSet<AuditKind> kindSet = EnumSet.noneOf(AuditKind.class);
        // уточняем по вызову get
        kindSet.addAll(Arrays.asList(kinds));
        kindSet.removeAll(KINDS_FOR_EDIT);
        if(kindSet.isEmpty()) {
            if(args.length>0) {
                return args[0]==null?CALL_FOR_ADD:CALL_FOR_EDIT;
            }
        }
        // уточняем по вызову save
        kindSet.clear();
        kindSet.addAll(Arrays.asList(kinds));
        kindSet.removeAll(KINDS_FOR_SAVE);
        if(kindSet.isEmpty()) {
            if(args.length>0) {
                for (int i = 0; i < method.getParameters().length; i++) {
                    Parameter p = method.getParameters()[i];
                    if(p.getAnnotation(RequestBody.class) != null) {
                        Field idField = OrmUtils.getIdField(p.getType(),false);
                        if(idField!=null) {
                            idField.setAccessible(true);
                            try {
                                Integer idValue = (Integer) idField.get(args[i]);
                                return idValue!=null?CALL_FOR_SAVE_UPDATE:CALL_FOR_SAVE_INSERT;
                            } catch (IllegalAccessException e) {
                                logger.warn(String
                                        .format("Audit threat! No access for %s field of % object", idField.toString(), args[i].toString()));
                                return UNTYPED;
                            }
                        }
                        logger.warn(String.format("Audit threat! @Id field not found in %s", p.getType().getName()));
                        return UNTYPED;
                    }
                }
            }
        }
        return UNTYPED;
    }

}
