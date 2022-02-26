package biz.gelicon.core.security;

public interface CredentialProvider<T> {

    public enum CredentialProviderType {
        AuthByPassword
    }

    public boolean checkAuthentication(Integer progUserId, T authData);
    public boolean checkTempAuthentication(Integer progUserId, T authData);
    public boolean hasTempAuthentication(Integer progUserId);
    public boolean updateAuthentication(Integer progUserId, T authData,boolean tempFlag);
}
