package com.vex.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 统一响应结果
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ApiResponse<T> {

    private String code;
    private String message;
    private T data;

    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<>("success", "ok", data);
    }

    public static <T> ApiResponse<T> error(String code, T data, String message) {
        return new ApiResponse<>(code, message, data);
    }

}