package biz.gelicon.core.security;

import biz.gelicon.core.repository.ProguserCredentialRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class CredentialProviderFactory {

    @Autowired
    ProguserCredentialRepository proguserCredentialRepository;

    public CredentialProvider getProvider(CredentialProvider.CredentialProviderType type) {
        switch (type) {
            case AuthByPassword:
                return new CredentialProviderByPassword(proguserCredentialRepository);
            default:
                throw new RuntimeException(String.format("Unknown credential provider type %s", type.toString()));
        }
    }

}
