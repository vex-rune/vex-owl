package com.vex.owl.user.user.auth.domain.code.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;
import org.springframework.data.redis.core.TimeToLive;

import java.io.Serial;
import java.io.Serializable;

@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
@RedisHash("code")  // 相当于 JPA @Table，指定 Redis key 前缀
public class CodeEntity implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Id
    private CodeId id;

    private String code;

    // 过期时间：单位 秒
    // 这里 = 5分钟过期
    @TimeToLive
    private Long ttl = 300L;

    public CodeEntity(CodeId id, String code) {
        this.id = id;
        this.code = code;
        this.ttl = 300L;
    }
}