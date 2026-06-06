package com.vex.owl.ai.domain.skills;

import com.vex.owl.ai.domain.skills.plan.Plan;
import lombok.NonNull;
import reactor.core.publisher.Flux;

public interface SkillExecutor<M,T> {
    SkillResult<M> execute(@NonNull String userMessage);
    SkillResult<T> executeStream(@NonNull String userMessage);


}
