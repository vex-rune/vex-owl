package com.vex.queries.jpa.model;

import com.vex.queries.jpa.id.BizSnowId;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.MappedSuperclass;

/**
 * 可识别实体基类
 * <p>继承自 {@link JpaBasicEntity}，增加主键 ID 字段</p>
 * <p>适用于需要唯一标识符的业务实体</p>
 *
 * @author vex
 * @since 2.0.0
 */
@MappedSuperclass
public abstract class JpaBasicWithIdEntity extends JpaBasicEntity {

    /**
     * ID 字段名称常量
     */
    public static final String ID_FIELD = "id";

    /**
     * 主键 ID - 自动生成
     */
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
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
