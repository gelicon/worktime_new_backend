package biz.gelicon.core.security;

import biz.gelicon.core.model.CapCode;
import biz.gelicon.core.model.ProguserCredential;
import biz.gelicon.core.repository.ProguserCredentialRepository;
import biz.gelicon.core.utils.SecurityUtils;

public class CredentialProviderByPassword implements CredentialProvider<String> {

    private final ProguserCredentialRepository proguserCredentialRepository;

    public CredentialProviderByPassword(ProguserCredentialRepository progUserRepository) {
        this.proguserCredentialRepository = progUserRepository;
    }

    @Override
    public boolean checkAuthentication(Integer progUserId, String authData) {
        ProguserCredential credential = proguserCredentialRepository.findByProguser(progUserId, CapCode.AUTH_BYPASSWORD, 0);
        if(credential!=null) {
            return SecurityUtils.checkPassword(authData,credential.getProguserCredentialPassword());
        }
        return false;
    }

    @Override
    public boolean checkTempAuthentication(Integer progUserId, String authData) {
        ProguserCredential credential = proguserCredentialRepository.findByProguser(progUserId, CapCode.AUTH_BYPASSWORD, 1);
        if(credential!=null) {
            return SecurityUtils.checkPassword(authData,credential.getProguserCredentialPassword());
        }
        return false;
    }

    @Override
    public boolean hasTempAuthentication(Integer progUserId) {
        ProguserCredential credential = proguserCredentialRepository.findByProguser(progUserId, CapCode.AUTH_BYPASSWORD, 1);
        return credential!=null;
    }


    @Override
    public boolean updateAuthentication(Integer progUserId, String authData, boolean tempFlag) {
        // если идет настоящий пароль, временный нужно удалить в любом случае, и наоборот
        // То ест удаляем противоположный пароль!
        ProguserCredential temp = proguserCredentialRepository.findByProguser(progUserId, CapCode.AUTH_BYPASSWORD, tempFlag?0:1);
        if(temp!=null) {
            proguserCredentialRepository.delete(temp.getProguserCredentialId());
        }
        ProguserCredential credential = proguserCredentialRepository
                .findByProguser(progUserId, CapCode.AUTH_BYPASSWORD, tempFlag?1:0);
        if(credential!=null) {
            credential.setProguserCredentialPassword(SecurityUtils.encodePassword(authData));
            proguserCredentialRepository.update(credential);
            return true;
        } else {
            credential = new ProguserCredential(
                    null,
                    SecurityUtils.encodePassword(authData),
                    progUserId,
                    CapCode.AUTH_BYPASSWORD,
                    0,
                    tempFlag?1:0
            );
            if(proguserCredentialRepository.insert(credential)>0) {
                return true;
            }
        }
        return false;
    }
}
