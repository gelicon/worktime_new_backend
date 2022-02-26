package biz.gelicon.core.security;

import biz.gelicon.core.model.AccessRole;
import biz.gelicon.core.model.Proguser;
import biz.gelicon.core.model.ProguserAuth;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class AuthenticationCashe {

    Map<String,CasheRecord> cashe = new HashMap<>();
    Map<String,Map<String,CasheRecord>> userIndex = new HashMap<>();

    public void put(String token,CasheRecord obj) {
        cashe.put(token,obj);
        Map<String, CasheRecord> idxMap = userIndex.get(obj.proguser.getProguserName());
        if(idxMap==null) {
            idxMap = new HashMap<>();
        }
        idxMap.put(token,obj);
        userIndex.put(obj.proguser.getProguserName(),idxMap);
    }

    public CasheRecord get(String token) {
        return cashe.get(token);
    }

    public void clearByUserName(String name) {
        Map<String, CasheRecord> idxMap = userIndex.get(name);
        if(idxMap==null) return;
        idxMap.keySet().forEach(tok->{
            cashe.remove(tok);
        });
        userIndex.remove(name);
    }

    public static class CasheRecord {
        private final ProguserAuth auth;
        private final Proguser proguser;
        private final List<AccessRole> roles;

        public CasheRecord(ProguserAuth auth, Proguser proguser, List<AccessRole> roles) {
            this.auth = auth;
            this.proguser = proguser;
            this.roles = roles;
        }

        public ProguserAuth getAuth() {
            return auth;
        }

        public Proguser getProguser() {
            return proguser;
        }

        public List<AccessRole> getRoles() {
            return roles;
        }
    }

}
