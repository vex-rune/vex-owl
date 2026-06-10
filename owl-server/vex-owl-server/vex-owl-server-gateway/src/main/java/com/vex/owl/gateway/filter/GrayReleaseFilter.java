package com.vex.owl.gateway.filter;

import com.vex.security.auth.AuthHeaderConstants;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpHeaders;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.Arrays;
import java.util.List;

/**
 * 灰度发布过滤器
 * <p>基于 Header 实现灰度路由，支持按用户维度染色</p>
 */
@Slf4j
@Component
public class GrayReleaseFilter implements GlobalFilter, Ordered {

    public static final String HEADER_GRAY_SWITCH = "X-Gray-Switch";
    public static final String HEADER_GRAY_VERSION = "X-Gray-Version";
    public static final String HEADER_GRAY_USERS = "X-Gray-Users";
    public static final int GRAY_FILTER_ORDER = -90;

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        String path = request.getURI().getPath();
        HttpHeaders headers = request.getHeaders();

        String graySwitch = headers.getFirst(HEADER_GRAY_SWITCH);
        String grayVersion = headers.getFirst(HEADER_GRAY_VERSION);
        String grayUsers = headers.getFirst(HEADER_GRAY_USERS);
        String currentUserId = headers.getFirst(AuthHeaderConstants.HEADER_USER_ID);

        if (!"on".equalsIgnoreCase(graySwitch)) {
            return chain.filter(exchange);
        }

        if (!isGrayUser(grayUsers, currentUserId)) {
            log.debug("用户不在灰度列表中 | 用户ID: {} | 灰度用户: {}", currentUserId, grayUsers);
            return chain.filter(exchange);
        }

        ServerHttpRequest.Builder builder = request.mutate()
                .header(HEADER_GRAY_SWITCH, graySwitch)
                .header(HEADER_GRAY_VERSION, grayVersion);

        if (StringUtils.hasText(grayUsers)) {
            builder.header(HEADER_GRAY_USERS, grayUsers);
        }

        log.debug("灰度请求 | 版本: {} | 用户ID: {} | 路径: {}", grayVersion, currentUserId, path);

        return chain.filter(exchange.mutate().request(builder.build()).build());
    }

    private boolean isGrayUser(String grayUsers, String currentUserId) {
        if (!StringUtils.hasText(grayUsers)) {
            return true;
        }
        if (!StringUtils.hasText(currentUserId)) {
            return false;
        }
        List<String> grayUserList = Arrays.asList(grayUsers.split(","));
        return grayUserList.stream().anyMatch(uid -> uid.trim().equals(currentUserId));
    }

    @Override
    public int getOrder() {
        return GRAY_FILTER_ORDER;
    }
}
