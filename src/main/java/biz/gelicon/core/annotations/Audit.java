package biz.gelicon.core.annotations;

import biz.gelicon.core.audit.AuditKind;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.TYPE,ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface Audit {
    AuditKind[] kinds();
    boolean noAuthentication() default false;
    boolean storeOutput() default true;
    boolean storeInput() default true;
}
