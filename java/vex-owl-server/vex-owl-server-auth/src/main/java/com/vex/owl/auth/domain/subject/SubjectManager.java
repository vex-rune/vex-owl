package com.vex.owl.auth.domain.subject;

/**
 * 主体管理
 * 负责主体信息的查询和管理
 */
public class SubjectManager {

    private final com.vex.owl.auth.domain.subject.repository.SubjectRepository subjectRepository;

    public SubjectManager(com.vex.owl.auth.domain.subject.repository.SubjectRepository subjectRepository) {
        this.subjectRepository = subjectRepository;
    }

    /**
     * 根据ID查询主体
     * @param id 主体ID
     * @return 主体信息
     */
    public com.vex.owl.auth.domain.subject.entity.Subject findById(Long id) {
        return subjectRepository.findById(id);
    }

    /**
     * 根据邮箱查询主体
     * @param email 邮箱
     * @return 主体信息
     */
    public com.vex.owl.auth.domain.subject.entity.Subject findByEmail(String email) {
        return subjectRepository.findByEmail(email);
    }

    /**
     * 条件查询主体（使用CriteriaQueryBuilder）
     * @param criteria 查询条件
     * @return 主体分页列表
     */
    public java.util.List<com.vex.owl.auth.domain.subject.entity.Subject> query(com.vex.query.criteria.QueriesCriteria criteria) {
        return java.util.Collections.emptyList();
    }

    /**
     * 创建主体
     * @param subject 主体信息
     */
    public void create(com.vex.owl.auth.domain.subject.entity.Subject subject) {
        subjectRepository.save(subject);
    }

    /**
     * 更新主体
     * @param subject 主体信息
     */
    public void update(com.vex.owl.auth.domain.subject.entity.Subject subject) {
        subjectRepository.save(subject);
    }

    /**
     * 删除主体
     * @param id 主体ID
     */
    public void delete(Long id) {
        subjectRepository.delete(id);
    }

    /**
     * 检查邮箱是否已存在
     * @param email 邮箱
     * @return 是否存在
     */
    public boolean existsByEmail(String email) {
        return subjectRepository.existsByEmail(email);
    }
}