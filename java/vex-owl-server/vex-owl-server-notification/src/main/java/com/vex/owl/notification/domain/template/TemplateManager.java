package com.vex.owl.notification.domain.template;

import com.vex.owl.notification.domain.template.entity.TemplateEntity;
import com.vex.owl.notification.domain.template.repo.TemplateRepository;
import com.vex.queries.model.queries.model.QueriesPageRequest;
import com.vex.queries.jpa.queries.JpaQueriesExecutor;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
@RequiredArgsConstructor
public class TemplateManager {

    private final TemplateRepository templateRepository;
    private final EntityManager entityManager;

    public Optional<TemplateEntity> findById(String id) {
        return templateRepository.findById(id);
    }

    public Optional<TemplateEntity> findByCode(String code) {
        return templateRepository.findByCode(code);
    }

    public List<TemplateEntity> query(QueriesPageRequest queriesPageRequest) {
        return JpaQueriesExecutor.of(TemplateEntity.class, entityManager)
                .page(queriesPageRequest);
    }

    public TemplateEntity create(TemplateEntity entity) {
        entity.setCreatedAt(LocalDateTime.now());
        entity.setUpdatedAt(LocalDateTime.now());
        entity.setEnabled(true);
        return templateRepository.save(entity);
    }

    public TemplateEntity update(TemplateEntity entity) {
        entity.setUpdatedAt(LocalDateTime.now());
        return templateRepository.save(entity);
    }

    public void delete(String id) {
        templateRepository.deleteById(id);
    }

    public String renderTemplate(String code, java.util.Map<String, String> params) {
        TemplateEntity template = findByCode(code)
                .orElseThrow(() -> new IllegalArgumentException("模板不存在: " + code));

        if (!template.getEnabled()) {
            throw new IllegalStateException("模板已禁用: " + code);
        }

        return render(template.getContent(), params);
    }

    public String render(String content, java.util.Map<String, String> params) {
        if (params == null || params.isEmpty()) {
            return content;
        }
        Pattern pattern = Pattern.compile("\\$\\{([^}]+)}");
        Matcher matcher = pattern.matcher(content);
        StringBuilder result = new StringBuilder();
        while (matcher.find()) {
            String key = matcher.group(1);
            String value = params.getOrDefault(key, matcher.group(0));
            matcher.appendReplacement(result, Matcher.quoteReplacement(value));
        }
        matcher.appendTail(result);
        return result.toString();
    }
}