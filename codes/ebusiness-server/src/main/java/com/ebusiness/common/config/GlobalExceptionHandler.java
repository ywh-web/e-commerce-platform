package com.ebusiness.common.config;

import com.ebusiness.common.util.ResponseResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import javax.servlet.http.HttpServletRequest;

/**
 * 全局异常处理器
 *
 * <p>避免异常堆栈直接抛给前端（那样前端只能看到 500 白屏）。
 * <p>统一转成 {@link ResponseResult} 结构返回，前端拦截器弹出友好提示。
 *
 * @author 杨伟宏
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * 业务异常（Service 层主动 throw 的自定义异常）
     */
    @ExceptionHandler(BusinessException.class)
    public ResponseResult<Object> handleBusinessException(BusinessException e, HttpServletRequest request) {
        log.warn("业务异常 [{}]：{}", request.getRequestURI(), e.getMessage());
        return ResponseResult.fail(e.getCode(), e.getMessage());
    }

    /**
     * 空指针异常
     */
    @ExceptionHandler(NullPointerException.class)
    public ResponseResult<Object> handleNullPointer(NullPointerException e, HttpServletRequest request) {
        log.error("空指针异常 [{}]", request.getRequestURI(), e);
        return ResponseResult.fail("数据不存在或参数缺失，请刷新后重试");
    }

    /**
     * 非法参数异常
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseResult<Object> handleIllegalArgument(IllegalArgumentException e, HttpServletRequest request) {
        log.warn("参数异常 [{}]：{}", request.getRequestURI(), e.getMessage());
        return ResponseResult.fail("参数不合法：" + e.getMessage());
    }

    /**
     * 兜底：其它所有未捕获异常
     */
    @ExceptionHandler(Exception.class)
    public ResponseResult<Object> handleException(Exception e, HttpServletRequest request) {
        log.error("系统异常 [{}]", request.getRequestURI(), e);
        return ResponseResult.fail("服务器内部错误，请稍后重试");
    }

    /**
     * 业务异常类 —— Service 层遇到可预期的业务错误时抛出
     */
    public static class BusinessException extends RuntimeException {
        private final int code;

        public BusinessException(String message) {
            super(message);
            this.code = ResponseResult.CODE_FAIL;
        }

        public BusinessException(int code, String message) {
            super(message);
            this.code = code;
        }

        public int getCode() {
            return code;
        }
    }
}
