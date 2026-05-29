package com.vex.queries.jpa.id;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@BizIdPrefix("CODE")  // 👈 任意前缀：CODE / USER / ORDER / SMS
@Table(name = "verify_code")
public class VerifyCode {

    @Id
    @BizSnowId       // 👈 只加这一个注解！搞定！
    private String id;

    // 你的业务字段
    private String identifier;
    private String codeType;
    private String code;
}