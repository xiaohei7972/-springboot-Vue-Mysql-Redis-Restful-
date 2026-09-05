package com.student.common;

import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.dao.DataAccessException;

@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(BusinessException.class)
    public ApiResponse<Void> business(BusinessException e) { return ApiResponse.error(e.getCode(), e.getMessage()); }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiResponse<Void> validation(MethodArgumentNotValidException e) {
        String message = e.getBindingResult().getFieldErrors().stream().findFirst()
                .map(x -> x.getField() + " " + x.getDefaultMessage()).orElse("参数校验失败");
        return ApiResponse.error(400, message);
    }

    @ExceptionHandler(AccessDeniedException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public ApiResponse<Void> denied() { return ApiResponse.error(403, "没有权限执行此操作"); }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ApiResponse<Void> other(Exception e) { return ApiResponse.error(500, "服务器内部错误"); }

    @ExceptionHandler(DataAccessException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public ApiResponse<Void> database(DataAccessException e) { return ApiResponse.error(409, "数据已存在或当前数据仍被业务引用"); }
}
