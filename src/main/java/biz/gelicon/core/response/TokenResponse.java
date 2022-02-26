package biz.gelicon.core.response;

public class TokenResponse {
    private String token;
    private UserInfo user;

    public TokenResponse() {
    }

    public TokenResponse(String token, String userName, String userLogin) {
        this.token = token;
        this.user = new UserInfo(userLogin,userName);
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public UserInfo getUser() {
        return user;
    }

    public void setUser(UserInfo user) {
        this.user = user;
    }

    public static class UserInfo {
        private String login;
        private String name;

        public UserInfo() {
        }

        public UserInfo(String login, String name) {
            this.login = login;
            this.name = name;
            if(name==null) {
                this.name = login;
            }
        }

        public String getLogin() {
            return login;
        }

        public void setLogin(String login) {
            this.login = login;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }
    }

}
