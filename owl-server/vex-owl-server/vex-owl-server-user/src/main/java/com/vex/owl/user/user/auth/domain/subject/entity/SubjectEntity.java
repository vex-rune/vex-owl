package com.vex.owl.user.user.auth.domain.subject.entity;

import com.vex.queries.jpa.id.BizIdPrefix;
import com.vex.queries.jpa.id.BizSnowId;
import com.vex.queries.jpa.model.JpaBasicEntity;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.util.Locale;

/**
 * 主体信息
 * 核心领域模型，代表系统中的用户主体
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Builder
@BizIdPrefix(value = "SUB")
@Table(name = "user_subject")
@EqualsAndHashCode(callSuper = false)
public class SubjectEntity extends JpaBasicEntity {

    @Id
    @BizSnowId
    private String id;

    @Column(length = 300)
    private String email;

    @Column(length = 100)
    private String nickname;

    @Column(length = 50)
    @Builder.Default
    private String role = "USER";

    public void setEmail(String email) {
        this.email = email.toLowerCase(Locale.ROOT);
    }
}