package com.vex.security.auth;

import lombok.Builder;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * 当前用户
 */
@Data
@Builder
public class CurrentUser implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 用户ID
     */
    private String subjectId;

    /**
     * 昵称
     */
    private String nickName;

    /**
     * 手机号
     */
    private String phone;

    /**
     * email
     */
    private String email;

    /**
     * 权限/角色集合
     */
    private Collection<String> authorities;

    /**
     * 角色
     */
    private String role;


    public Map<String, Object> toMap() {
        HashMap<String, Object> map = new HashMap<>();
        map.put("subjectId", subjectId);
        map.put("nickName", nickName);
        map.put("phone", phone);
        map.put("email", email);
        map.put("authorities", authorities);
        return map;
    }
}