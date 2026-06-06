package com.vex.owl.ai.domain.llm.exception;

/**
 * 模型领域异常
 */
public class ModelException extends RuntimeException {

    private final String code;

    public ModelException(String message) {
        super(message);
        this.code = null;
    }

    public ModelException(String code, String message) {
        super(message);
        this.code = code;
    }

    public String getCode() {
        return code;
    }
}
