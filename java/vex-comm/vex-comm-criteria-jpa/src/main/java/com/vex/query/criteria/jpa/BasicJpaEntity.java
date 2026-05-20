package com.vex.query.criteria.jpa;

import jakarta.persistence.EntityListeners;
import jakarta.persistence.MappedSuperclass;
import lombok.Data;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.io.Serializable;
import java.util.Date;

/**
 * 基础 JPA 实体类
 * <p>提供审计字段支持，包括创建时间、更新时间、创建人、更新人和删除状态</p>
 * <p>所有需要审计功能的实体都应继承此类</p>
 *
 * @author vex
 * @since 2.0.0
 */
@Data
@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
public abstract class BasicJpaEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 创建时间 - 自动填充
     */
    @CreatedDate
    private Date createTime;

    /**
     * 更新时间 - 自动更新
     */
    @LastModifiedDate
    private Date updateTime;

    /**
     * 创建主体（用户/系统）
     */
    @CreatedBy
    private String createSubject;

    /**
     * 更新主体（用户/系统）
     */
    @LastModifiedBy
    private String updateSubject;

    /**
     * 逻辑删除标记
     */
    private Boolean deleted = false;
}
