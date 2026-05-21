package com.vex.owl.notification.domain.template.entity;

import com.vex.queries.jpa.id.BizIdPrefix;
import com.vex.queries.jpa.id.BizSnowId;
import com.vex.queries.jpa.model.JpaBasicWithIdEntity;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
@BizIdPrefix(value = "template")
@Table(name = "notification_template")
public class TemplateEntity extends JpaBasicWithIdEntity {

    @Id
    @BizSnowId
    private String id;

    @Column(nullable = false)
    @NotBlank
    @Size(max = 100)
    private String name;

    @Column(nullable = false, unique = true)
    @NotBlank
    @Size(max = 50)
    private String code;

    @Column(nullable = false, columnDefinition = "TEXT")
    @NotBlank
    private String content;

    @Column(columnDefinition = "TEXT")
    private String remark;

    @Column(nullable = false)
    private Boolean enabled;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;
}