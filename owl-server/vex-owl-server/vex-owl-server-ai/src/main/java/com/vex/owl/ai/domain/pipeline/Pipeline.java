package com.vex.owl.ai.domain.pipeline;

import com.vex.owl.ai.domain.context.RunContext;

/**
 * Pipeline 接口
 *
 * <p>管道接口，定义执行流程</p>
 */
public interface Pipeline {

    /**
     * 执行管道
     *
     * @param input   输入
     * @param context 上下文
     * @return 输出结果
     */
    String execute(String input, RunContext context);
}
