package com.vex.owl.user.user.auth.domain.subject.entity;

import com.vex.queries.jpa.id.BizIdPrefix;
import com.vex.queries.jpa.id.BizSnowId;
import com.vex.queries.jpa.model.JpaBasicWithIdEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

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
@Table(name = "auth_subject")
public class SubjectEntity extends JpaBasicWithIdEntity {

    @Id
    @BizSnowId
    private String id;

    @Column(nullable = false, unique = true, length = 255)
    private String email;

    @Column(length = 100)
    private String nickname;

    @Column(nullable = false, length = 50)
    private String role = "USER";
}