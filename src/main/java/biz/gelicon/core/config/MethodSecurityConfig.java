package biz.gelicon.core.config;

import biz.gelicon.core.security.RolePermissionEvaluator;
import biz.gelicon.core.security.UserDetailsImpl;
import org.aopalliance.intercept.MethodInvocation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.security.access.expression.method.DefaultMethodSecurityExpressionHandler;
import org.springframework.security.access.expression.method.MethodSecurityExpressionHandler;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.method.configuration.GlobalMethodSecurityConfiguration;
import org.springframework.security.core.Authentication;

@Configuration
@EnableGlobalMethodSecurity(securedEnabled = true,prePostEnabled = true)
public class MethodSecurityConfig extends GlobalMethodSecurityConfiguration {
    @Autowired
    private RolePermissionEvaluator permissionEvaluator;

    static class CustomMethodSecurityExpressionHandler extends DefaultMethodSecurityExpressionHandler {
        @Override
        public StandardEvaluationContext createEvaluationContextInternal(Authentication auth, MethodInvocation mi) {
            ((UserDetailsImpl)auth.getPrincipal()).setMethodInvocation(mi);
            return super.createEvaluationContextInternal(auth, mi);
        }
    }

    @Override
    protected MethodSecurityExpressionHandler createExpressionHandler() {
        DefaultMethodSecurityExpressionHandler expressionHandler = new CustomMethodSecurityExpressionHandler();
        expressionHandler.setPermissionEvaluator(permissionEvaluator);
        return expressionHandler;
    }
}