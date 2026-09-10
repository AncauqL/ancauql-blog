package com.example.blog_backend.config;

import com.example.blog_backend.common.Result;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * 全局异常兜底：避免把 500 白页/堆栈直接抛给前端。
 * 统一按项目约定返回 {code:'500', msg:'服务器内部错误'}，细节只记服务端日志。
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log =
            LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(IllegalArgumentException.class)
    public Result handleIllegalArgument(IllegalArgumentException e) {
        // 业务校验类异常：把可读原因回给前端
        return Result.error(e.getMessage() == null ? "请求参数不正确" : e.getMessage());
    }

    @ExceptionHandler(Exception.class)
    public Result handleException(Exception e) {
        log.error("未处理的异常: {}", e.getMessage(), e);
        return Result.error("服务器内部错误，请稍后再试");
    }
}
