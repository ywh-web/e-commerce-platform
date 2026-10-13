package com.ebusiness.common.util;

import cn.hutool.captcha.CaptchaUtil;
import cn.hutool.captcha.LineCaptcha;
import cn.hutool.core.util.IdUtil;
// 注意：本类名为 CaptchaCodeUtil，与 Hutool 的 cn.hutool.captcha.CaptchaUtil 不同名，
// 因此上面的 import 可以正常使用，不会产生类名冲突。
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * 图形验证码工具
 *
 * <p>生成流程：
 * <ol>
 *   <li>Hutool 生成验证码图片与答案</li>
 *   <li>答案存进 Redis，key 为 {@code CODE_{uuid}}，有效期 5 分钟</li>
 *   <li>把 uuid 与图片的 Base64 返回前端；前端提交时带上 uuid，
 *       后端凭 uuid 去 Redis 取答案比对</li>
 * </ol>
 *
 * <p>为什么用 Redis 而不是 session？前后端分离下后端通常无状态，
 * 用 Redis 存验证码天然支持多实例部署，也更符合工程实践。
 *
 * @author 杨伟宏
 */
@Component
public class CaptchaCodeUtil {

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    /** Redis 中验证码 key 的前缀 */
    @Value("${ebusiness.redis.code-prefix}")
    private String codePrefix;

    /** 验证码有效期（分钟） */
    private static final long EXPIRE_MINUTES = 5;

    /** 验证码图片宽度 */
    private static final int WIDTH = 120;
    /** 验证码图片高度 */
    private static final int HEIGHT = 40;
    /** 验证码字符个数 */
    private static final int CODE_COUNT = 4;
    /** 干扰线数量 */
    private static final int LINE_COUNT = 30;

    /**
     * 生成图形验证码
     *
     * @return 包含 uuid（验证码唯一标识）与 img（Base64 图片，可直接给 img 标签的 src 用）
     */
    public Map<String, String> generate() {
        LineCaptcha captcha = CaptchaUtil.createLineCaptcha(WIDTH, HEIGHT, CODE_COUNT, LINE_COUNT);
        String code = captcha.getCode();
        String uuid = IdUtil.fastSimpleUUID();

        // 答案写进 Redis，5 分钟过期
        redisTemplate.opsForValue().set(codePrefix + uuid, code, EXPIRE_MINUTES, TimeUnit.MINUTES);

        Map<String, String> result = new HashMap<>(4);
        result.put("uuid", uuid);
        result.put("img", captcha.getImageBase64Data());
        // 开发期间把答案也返回，方便调试；正式环境请去掉这一行
        result.put("code", code);
        return result;
    }

    /**
     * 校验验证码
     *
     * @param uuid  验证码唯一标识
     * @param input 用户输入的验证码
     * @return true 表示正确（校验成功后会删除 Redis 中的答案，防止重复使用）
     */
    public boolean verify(String uuid, String input) {
        if (uuid == null || input == null || input.trim().isEmpty()) {
            return false;
        }
        String key = codePrefix + uuid;
        Object cached = redisTemplate.opsForValue().get(key);
        if (cached == null) {
            return false;
        }
        // 校验后立即失效，防止暴力重试
        redisTemplate.delete(key);
        return cached.toString().equalsIgnoreCase(input.trim());
    }
}
