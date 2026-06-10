package com.vex.security.auth;

import lombok.Builder;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * JWT 认证用户
 */
@Data
@Builder
public class AuthUser implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private String subjectId;
    private String nickName;
    private String phone;
    private String email;
    private Collection<String> authorities;
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