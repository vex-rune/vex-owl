package com.vex.owl.notification.api;

import com.vex.owl.notification.domain.template.TemplateManager;
import com.vex.owl.notification.domain.template.entity.TemplateEntity;
import com.vex.owl.notification.api.request.TemplateCreateRequest;
import com.vex.owl.notification.api.request.TemplateUpdateRequest;
import com.vex.model.ApiResponse;
import com.vex.queries.model.queries.model.QueriesPageRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;

import java.util.List;

/**
 * 通知模版模块
 * <p>模板管理相关业务接口</p>
 */
@RestController
@RequestMapping("/api/v1/notification/template")
@RequiredArgsConstructor
public class TemplateApi {

    private final TemplateManager templateManager;

    /**
     * 通知-模板通用查询
     * <p>分页+多条件组合查询</p>
     */
    @PostMapping("/query")
    public ApiResponse<List<TemplateEntity>> query(@RequestBody QueriesPageRequest request) {
        return ApiResponse.success(templateManager.query(request));
    }

    /**
     * 通知-模板查询单个
     * <p>根据ID查询模板详情</p>
     */
    @GetMapping("/{id}")
    public ApiResponse<TemplateEntity> get(@PathVariable String id) {
        return ApiResponse.success(templateManager.findById(id).orElse(null));
    }

    /**
     * 通知-模板查询单个
     * <p>根据编码查询模板详情</p>
     */
    @GetMapping("/code/{code}")
    public ApiResponse<TemplateEntity> getByCode(@PathVariable String code) {
        return ApiResponse.success(templateManager.findByCode(code).orElse(null));
    }

    /**
     * 通知-模板新增数据
     * <p>创建新模板</p>
     */
    @PostMapping
    public ApiResponse<TemplateEntity> create(@Valid @RequestBody TemplateCreateRequest request) {
        TemplateEntity entity = TemplateEntity.builder()
                .name(request.getName())
                .code(request.getCode())
                .content(request.getContent())
                .remark(request.getRemark())
                .build();
        return ApiResponse.success(templateManager.create(entity));
    }

    /**
     * 通知-模板修改数据
     * <p>根据ID更新模板信息</p>
     */
    @PutMapping("/{id}")
    public ApiResponse<TemplateEntity> update(@PathVariable String id,
                                            @Valid @RequestBody TemplateUpdateRequest request) {
        TemplateEntity existing = templateManager.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("模板不存在"));

        if (request.getName() != null) {
            existing.setName(request.getName());
        }
        if (request.getContent() != null) {
            existing.setContent(request.getContent());
        }
        if (request.getRemark() != null) {
            existing.setRemark(request.getRemark());
        }
        if (request.getEnabled() != null) {
            existing.setEnabled(request.getEnabled());
        }

        return ApiResponse.success(templateManager.update(existing));
    }

    /**
     * 通知-模板删除数据
     * <p>根据ID删除模板</p>
     */
    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable String id) {
        templateManager.delete(id);
        return ApiResponse.success();
    }
}