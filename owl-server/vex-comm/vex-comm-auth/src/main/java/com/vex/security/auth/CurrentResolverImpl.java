package com.vex.security.auth;

import com.vex.event.CurrentTrace;
import com.vex.event.CurrentUser;
import com.vex.event.CurrentResolver;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.Optional;

/**
 * CurrentResolver 实现
 *
 * <p>从 HTTP 请求头中读取用户信息（由网关转发）。</p>
 */
public class CurrentResolverImpl implements CurrentResolver {

    @Override
    public Optional<CurrentUser> resolveCurrentUser() {
        HttpServletRequest request = getRequest();
        if (request == null) {
            return Optional.of(CurrentUser.anonymous());
        }

        String userId = request.getHeader(AuthHeaderConstants.HEADER_USER_ID);
        String userName = request.getHeader(AuthHeaderConstants.HEADER_USER_NAME);
        String userGroup = request.getHeader(AuthHeaderConstants.HEADER_USER_GROUP);
        String loginTime = request.getHeader(AuthHeaderConstants.HEADER_LOGIN_TIME);
        String role = request.getHeader(AuthHeaderConstants.HEADER_ROLE);
        String email = request.getHeader(AuthHeaderConstants.HEADER_EMAIL);
        String nickname = request.getHeader(AuthHeaderConstants.HEADER_NICKNAME);

        return Optional.of(CurrentUser.builder()
                .authEnabled(userId != null && !userId.isBlank())
                .userId(userId != null ? userId : "")
                .userName(userName != null ? userName : "")
                .userGroup(userGroup != null ? userGroup : AuthHeaderConstants.DEFAULT_USER_GROUP)
                .loginTime(loginTime != null ? loginTime : AuthHeaderConstants.DEFAULT_LOGIN_TIME)
                .role(role != null ? role : "")
                .email(email != null ? email : "")
                .nickname(nickname != null ? nickname : "")
                .build());
    }

    @Override
    public Optional<CurrentTrace> resolveCurrentTrace() {
        HttpServletRequest request = getRequest();
        if (request == null) {
            return Optional.of(CurrentTrace.anonymous());
        }

        String traceId = request.getHeader(AuthHeaderConstants.HEADER_TRACE_ID);
        String sessionId = request.getHeader(AuthHeaderConstants.HEADER_SESSION_ID);

        return Optional.of(CurrentTrace.builder()
                .sessionId(sessionId)
                .traceId(traceId)
                .build());
    }

    private HttpServletRequest getRequest() {
        ServletRequestAttributes attrs = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        return attrs != null ? attrs.getRequest() : null;
    }
}