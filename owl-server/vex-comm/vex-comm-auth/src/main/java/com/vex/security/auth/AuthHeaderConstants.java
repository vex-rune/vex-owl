package com.vex.security.auth;

public final class AuthHeaderConstants {

    private AuthHeaderConstants() {}

    public static final String HEADER_AUTH_ENABLED = "X-Auth-Enabled";
    public static final String HEADER_USER_ID = "X-User-Id";
    public static final String HEADER_USER_NAME = "X-User-Name";
    public static final String HEADER_USER_GROUP = "X-User-Group";
    public static final String HEADER_LOGIN_TIME = "X-Login-Time";
    public static final String HEADER_AUTH_TOKEN = "X-Auth-Token";
    public static final String HEADER_ROLE = "X-User-Role";
    public static final String HEADER_EMAIL = "X-User-Email";
    public static final String HEADER_NICKNAME = "X-User-Nickname";

    public static final String DEFAULT_USER_GROUP = "default";
    public static final String DEFAULT_LOGIN_TIME = "0";

    public static final String AUTH_ENABLED_TRUE = "true";
    public static final String AUTH_ENABLED_FALSE = "false";
}