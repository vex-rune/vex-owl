package com.vex.owl.ai.api.admin;

import java.util.List;

import com.vex.model.ApiResponse;
import com.vex.owl.ai.domain.llm.entity.ModelEntity;
import com.vex.owl.ai.domain.llm.repo.AiModelRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * AI 模型管理接口（管理后台）
 *
 * <p>提供模型的 CRUD 操作，用于管理后台配置 AI 模型。</p>
 */
@RestController
@RequestMapping("/api/ai/admin/models")
@RequiredArgsConstructor
public class AiModelAdminApi {

    private final AiModelRepository modelRepository;

    /**
     * 模型-查询所有
     */
    @GetMapping
    public ApiResponse<List<ModelEntity>> list(
            @RequestHeader("X-User-Id") String userId) {
        return ApiResponse.success(modelRepository.findByUserId(userId));
    }

    /**
     * 模型-查询指定模型
     */
    @GetMapping("/{id}")
    public ApiResponse<ModelEntity> get(@PathVariable String id) {
        return modelRepository.findById(id)
                .map(ApiResponse::success)
                .orElse(ApiResponse.error("MODEL_NOT_FOUND", null, "模型不存在: " + id));
    }

    /**
     * 模型-新增
     */
    @PostMapping
    public ApiResponse<ModelEntity> create(@RequestBody ModelEntity entity) {
        return ApiResponse.success(modelRepository.save(entity));
    }

    /**
     * 模型-更新
     */
    @PutMapping("/{id}")
    public ApiResponse<ModelEntity> update(@PathVariable String id, @RequestBody ModelEntity entity) {
        if (!modelRepository.existsById(id)) {
            return ApiResponse.error("MODEL_NOT_FOUND", null, "模型不存在: " + id);
        }
        entity.setId(id);
        return ApiResponse.success(modelRepository.save(entity));
    }

    /**
     * 模型-删除指定模型
     */
    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable String id) {
        modelRepository.deleteById(id);
        return ApiResponse.success(null);
    }
}
