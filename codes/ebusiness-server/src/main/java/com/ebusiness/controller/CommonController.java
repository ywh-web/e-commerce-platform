package com.ebusiness.controller;

import com.ebusiness.common.util.CaptchaCodeUtil;
import com.ebusiness.common.util.ResponseResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * 公共接口控制器（图形验证码等）
 *
 * <p>接口前缀：/api/common
 * <p>本控制器接口位于拦截器白名单，无需登录即可访问。
 *
 * @author 杨伟宏
 */
@RestController
@RequestMapping("/api/common")
public class CommonController {

    @Autowired
    private CaptchaCodeUtil captchaUtil;

    /**
     * 获取图形验证码
     *
     * <p>GET /api/common/captcha
     * <p>返回：{ "code": 200, "data": { "uuid": "...", "img": "data:image/png;base64,..." } }
     *
     * <p>前端拿到后：把 img 直接赋给 el-image / img 的 src，
     * 把 uuid 随登录请求一起提交，后端凭 uuid 去 Redis 取答案比对。
     */
    @GetMapping("/captcha")
    public ResponseResult<Object> captcha() {
        Map<String, String> data = captchaUtil.generate();
        return ResponseResult.success(data);
    }
}
