package com.vex.model;

import feign.FeignException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.nio.charset.StandardCharsets;
import java.util.stream.Collectors;

@RestControllerAdvice
@Order(Ordered.HIGHEST_PRECEDENCE)
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(VexException.class)
    @ResponseStatus(HttpStatus.OK)
    public ApiResponse<Void> handleBusinessException(VexException ex) {
        log.warn("业务异常 | code: {} | message: {}", ex.getCode(), ex.getMessage());
        return ApiResponse.error(ex.getCode(), null, ex.getMessage());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiResponse<Void> handleValidationException(MethodArgumentNotValidException ex) {
        String message = ex.getBindingResult().getFieldErrors().stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.joining("; "));
        log.warn("参数校验失败 | message: {}", message);
        return ApiResponse.error("VALIDATION_ERROR", null, message);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiResponse<Void> handleIllegalArgument(IllegalArgumentException ex) {
        log.warn("参数非法 | message: {}", ex.getMessage(), ex);
        return ApiResponse.error("ILLEGAL_ARGUMENT", null, ex.getMessage());
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ApiResponse<Void> handleException(Exception ex) {
        log.error("系统异常 message:{}", ex.getMessage(), ex);
        return ApiResponse.error("INTERNAL_ERROR", null, "服务器内部错误");
    }

    @ExceptionHandler(FeignException.class)
    public ApiResponse<Void> handleFeignException(FeignException ex) {
        int status = ex.status();
        String url = ex.request() != null ? ex.request().url() : "unknown";
        String body = extractBody(ex);

        log.error("Feign 调用失败 | status: {} | url: {} | body: {}", status, url, body);

        String message = switch (status) {
            case 404 -> "下游服务接口不存在: " + url;
            case 502, 503 -> "下游服务不可用: " + url;
            default -> "下游服务调用异常: " + ex.getMessage();
        };

        return ApiResponse.error(String.valueOf(status), null, message);
    }

    private String extractBody(FeignException ex) {
        if (ex.responseBody().isPresent()) {
            byte[] bytes = ex.responseBody().get().array();
            return new String(bytes, StandardCharsets.UTF_8);
        }
        return null;
    }
}
