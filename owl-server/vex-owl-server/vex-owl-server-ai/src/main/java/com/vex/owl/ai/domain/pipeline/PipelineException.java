package com.vex.owl.ai.domain.pipeline;

/**
 * Pipeline 异常
 */
public class PipelineException extends RuntimeException {

    public PipelineException(String message) {
        super(message);
    }

    public PipelineException(String message, Throwable cause) {
        super(message, cause);
    }
}
