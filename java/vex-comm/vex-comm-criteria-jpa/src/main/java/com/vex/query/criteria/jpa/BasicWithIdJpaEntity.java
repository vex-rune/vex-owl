package com.vex.query.criteria.jpa;

import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.MappedSuperclass;

/**
 * 可识别实体基类
 * <p>继承自 {@link BasicJpaEntity}，增加主键 ID 字段</p>
 * <p>适用于需要唯一标识符的业务实体</p>
 *
 * @author vex
 * @since 2.0.0
 */
@MappedSuperclass
public abstract class BasicWithIdJpaEntity extends BasicJpaEntity {

    /**
     * ID 字段名称常量
     */
    public static final String ID_FIELD = "id";

    /**
     * 主键 ID - 自动生成
     */
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private String id;

    /**
     * 获取实体 ID
     *
     * @return 实体 ID
     */
    public String getId() {
        return id;
    }

    /**
     * 设置实体 ID
     *
     * @param id 实体 ID
     */
    public void setId(String id) {
        this.id = id;
    }
}
