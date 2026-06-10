package com.vex.security.auth;

public final class AuthHeaderConstants {

    private AuthHeaderConstants() {}

    public static final String HEADER_AUTH_ENABLED = "Vex-Auth-Enabled";
    public static final String HEADER_USER_ID = "Vex-User-Id";
    public static final String HEADER_USER_NAME = "Vex-User-Name";
    public static final String HEADER_USER_GROUP = "Vex-User-Group";
    public static final String HEADER_LOGIN_TIME = "Vex-Login-Time";
    public static final String HEADER_AUTH_TOKEN = "Vex-Auth-Token";
    public static final String HEADER_ROLE = "Vex-User-Role";
    public static final String HEADER_EMAIL = "Vex-User-Email";
    public static final String HEADER_NICKNAME = "Vex-User-Nickname";
    public static final String HEADER_SESSION_ID = "Vex-Session-Id";
    public static final String HEADER_TRACE_ID = "Vex-Trace-Id";

    public static final String DEFAULT_USER_GROUP = "default";
    public static final String DEFAULT_LOGIN_TIME = "0";

    public static final String AUTH_ENABLED_TRUE = "true";
    public static final String AUTH_ENABLED_FALSE = "false";
}
