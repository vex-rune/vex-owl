package com.vex.event;

import java.util.Optional;

/**
 * 当前用户上下文解析器（由外部模块实现）
 *
 * <p>event 模块不依赖 web，通过此接口获取当前请求的用户信息。</p>
 */
public interface CurrentResolver {

    Optional<CurrentUser> resolveCurrentUser();

    Optional<CurrentTrace> resolveCurrentTrace();

}