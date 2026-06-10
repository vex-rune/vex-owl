package com.vex.security.jwt;

public final class JwtClaimConstants {

    private JwtClaimConstants() {}

    public static final String VEX_SYSTEM = "vex-owl";

    public static final String TOKEN_TYPE_ACCESS = "access";
    public static final String TOKEN_TYPE_REFRESH = "refresh";

    public static final long DEFAULT_ACCESS_TOKEN_VALIDITY = 3600L;
    public static final long DEFAULT_REFRESH_TOKEN_VALIDITY = 604800L;

    public static final String CLAIM_TYPE = "type";
    public static final String CLAIM_CLIENT_ID = "client_id";
    public static final String CLAIM_SCOPES = "scope";
    public static final String CLAIM_USER_ID = "userId";
    public static final String CLAIM_USER_NAME = "userName";
    public static final String CLAIM_USER_GROUP = "userGroup";
    public static final String CLAIM_LOGIN_TIME = "loginTime";
    public static final String CLAIM_EMAIL = "email";
    public static final String CLAIM_ROLE = "role";
    public static final String CLAIM_NICKNAME = "nickname";
    public static final String CLAIM_AUTHORITIES = "authorities";

    public static final String ROLE_ADMIN = "ADMIN";
    public static final String ROLE_USER = "USER";
    public static final String ROLE_GUEST = "GUEST";

    public static final String LOGIN_TYPE_ADMIN = "admin";
    public static final String LOGIN_TYPE_EMAIL_PASSWORD = "email_password";
    public static final String LOGIN_TYPE_EMAIL_CODE = "email_code";
    public static final String LOGIN_TYPE_INTERNAL = "internal";
}