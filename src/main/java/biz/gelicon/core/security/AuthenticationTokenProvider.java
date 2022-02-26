package biz.gelicon.core.security;

import biz.gelicon.core.jobs.JobDispatcher;
import biz.gelicon.core.jobs.TokenRunnable;
import biz.gelicon.core.model.AccessRole;
import biz.gelicon.core.model.Proguser;
import biz.gelicon.core.model.ProguserAuth;
import biz.gelicon.core.repository.AccessRoleRepository;
import biz.gelicon.core.repository.ProgUserAuthRepository;
import biz.gelicon.core.repository.ProgUserRepository;
import biz.gelicon.core.response.exceptions.TokenExpiredException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.authentication.dao.AbstractUserDetailsAuthenticationProvider;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.authentication.www.NonceExpiredException;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.List;

@Component
public class AuthenticationTokenProvider extends AbstractUserDetailsAuthenticationProvider {
    @Autowired
    ProgUserRepository progUserRepository;
    @Autowired
    AccessRoleRepository accessRoleRepository;

    @Autowired
    ProgUserAuthRepository progUserAuthRepository;
    @Autowired
    AuthenticationCashe authenticationCashe;
    @Autowired(required = false)
    private StartUserAuthentication startUserAuthentication;

    @Autowired
    @Qualifier("jobTokenUpdate")
    private JobDispatcher jobTokenUpdate;

    @Override
    protected void additionalAuthenticationChecks(UserDetails userDetails, UsernamePasswordAuthenticationToken usernamePasswordAuthenticationToken) throws AuthenticationException {
        //
    }

    @Override
    protected UserDetails retrieveUser(String userName, UsernamePasswordAuthenticationToken usernamePasswordAuthenticationToken) throws AuthenticationException {
        final String token = (String)usernamePasswordAuthenticationToken.getCredentials();

        // проверка токена
        AuthenticationCashe.CasheRecord cacheRecord = authenticationCashe.get(token);
        try {
            if(cacheRecord==null) {
                cacheRecord = requestAuthentication(token);
                authenticationCashe.put(token,cacheRecord);
            } else {
                cacheRecord.getAuth().checkExpired();
            }
        } catch (TokenExpiredException ex) {
            throw new NonceExpiredException(ex.getMessage());
        }
        // обновляем время последнего доступа к токену асинхронно, через JobToken и Rx
        final Date now = new Date();
        final AuthenticationCashe.CasheRecord safeCacheRecord = cacheRecord;
        jobTokenUpdate.pushJob(new TokenRunnable() {
            @Override
            public String getToken() {
                return token;
            }

            @Override
            public void run() {
                progUserAuthRepository.updateAccessToken(safeCacheRecord.getAuth(),now);
            }
        });

        return new UserDetailsImpl(cacheRecord.getProguser(),cacheRecord.getRoles());
    }

    private AuthenticationCashe.CasheRecord requestAuthentication(String token) {
        ProguserAuth auth = progUserAuthRepository.findByValue(token);
        if(auth==null) {
            throw new UsernameNotFoundException(String.format("Token %s not found", token));
        }
        auth.checkExpired();
        // получаем пользователя
        Proguser pu = progUserRepository.findByToken(token);
        if(pu == null || !pu.toUserDetail().isAccountNonLocked()) {
            throw new UsernameNotFoundException(String.format("Cannot find user with authentication token %s", token));
        }
        List<AccessRole> roles = refreshAuthorizationInfo(pu);
        return new AuthenticationCashe.CasheRecord(auth,pu,roles);
    }

    public List<AccessRole> refreshAuthorizationInfo(Proguser pu) {
        // получаем список ролей
        List<AccessRole> roles = accessRoleRepository.findByUser(pu.getProguserId());
        // получаем разные атрибуты
        // вызываем событие старта
        if(startUserAuthentication!=null) {
            startUserAuthentication.onStart(pu);
        }
        return roles;
    }

}
