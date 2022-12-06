package cn.roamblue.cloud.management.util;

public class Oauth2 {
    public static final class Param {
        public static final String CLIENT_ID = "client_id";
        public static final String CLIENT_SECRET = "client_secret";
        public static final String CODE = "code";
        public static final String TOKEN = "token";
        public static final String GRANT_TYPE = "grant_type";
    }

    public static final class GrantType {
        public static final String AUTHORIZATION_CODE = "authorization_code";
    }
}
