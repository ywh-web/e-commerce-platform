package com.ebusiness.common.util;

import lombok.Data;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * 统一响应结果封装
 *
 * <p>全站所有接口都返回这个结构，前端只需判断 code 即可统一处理：
 * <pre>
 * {
 *   "code": 200,
 *   "msg": "操作成功",
 *   "data": { ... }
 * }
 * </pre>
 *
 * <p>约定：200 表示成功，其余为失败。前端 Axios 响应拦截器按此判断。
 *
 * @param <T> 业务数据类型
 * @author 杨伟宏
 */
@Data
public class ResponseResult<T> implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 成功状态码 */
    public static final int CODE_SUCCESS = 200;
    /** 失败状态码（业务逻辑失败） */
    public static final int CODE_FAIL = 500;
    /** 未登录 / token 失效状态码（前端拦截器据此踢回登录页） */
    public static final int CODE_UNAUTHORIZED = 401;
    /** 无权限状态码 */
    public static final int CODE_FORBIDDEN = 403;

    /** 状态码 */
    private Integer code;

    /** 提示信息 */
    private String msg;

    /** 业务数据 */
    private T data;

    public ResponseResult() {
    }

    public ResponseResult(Integer code, String msg, T data) {
        this.code = code;
        this.msg = msg;
        this.data = data;
    }

    // ==================== 成功 ====================

    /**
     * 成功（无数据）
     */
    public static <T> ResponseResult<T> success() {
        return new ResponseResult<>(CODE_SUCCESS, "操作成功", null);
    }

    /**
     * 成功（带数据）
     */
    public static <T> ResponseResult<T> success(T data) {
        return new ResponseResult<>(CODE_SUCCESS, "操作成功", data);
    }

    /**
     * 成功（自定义提示 + 数据）
     */
    public static <T> ResponseResult<T> success(String msg, T data) {
        return new ResponseResult<>(CODE_SUCCESS, msg, data);
    }

    // ==================== 失败 ====================

    /**
     * 失败（默认 500 + 自定义提示）
     */
    public static <T> ResponseResult<T> fail(String msg) {
        return new ResponseResult<>(CODE_FAIL, msg, null);
    }

    /**
     * 失败（自定义状态码 + 提示）
     */
    public static <T> ResponseResult<T> fail(Integer code, String msg) {
        return new ResponseResult<>(code, msg, null);
    }

    /** 未登录 / token 失效 */
    public static <T> ResponseResult<T> unauthorized(String msg) {
        return new ResponseResult<>(CODE_UNAUTHORIZED, msg, null);
    }

    /** 无权限 */
    public static <T> ResponseResult<T> forbidden(String msg) {
        return new ResponseResult<>(CODE_FORBIDDEN, msg, null);
    }

    // ==================== 教材风格兼容方法 ====================

    /**
     * 返回"消息型"结果 —— 与教材 {@code getMessageResult} 语义一致
     *
     * <p>data 中携带一个 code 字段（如 A001 表示成功），便于前端做细粒度判断。
     *
     * @param data 一般为 null，或需要回传的实体
     * @param biz 业务码：A001 成功 / A002·A003 各类失败
     */
    public static <T> ResponseResult<T> getMessageResult(T data, String biz) {
        Map<String, Object> payload = new HashMap<>(2);
        payload.put("bizCode", biz);
        if (data != null) {
            payload.put("data", data);
        }
        boolean ok = "A001".equals(biz);
        return new ResponseResult<>(ok ? CODE_SUCCESS : CODE_FAIL, ok ? "操作成功" : "操作失败", (T) payload);
    }

    /**
     * 返回"数据型"结果 —— 与教材 {@code getSuccessResult} 语义一致
     */
    public static <T> ResponseResult<T> getSuccessResult(T data) {
        return new ResponseResult<>(CODE_SUCCESS, "操作成功", data);
    }
}
