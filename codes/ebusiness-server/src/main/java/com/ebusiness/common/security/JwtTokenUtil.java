package com.ebusiness.common.security;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.jwt.JWT;
import cn.hutool.jwt.JWTUtil;
import cn.hutool.jwt.JWTValidator;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * JWT Token 工具类
 *
 * <p>登录成功后由本类签发 token，前端存进 sessionStorage，
 * 之后每次请求由 Axios 拦截器放进 {@code Authorization: Bearer xxx} 请求头。
 * <p>后端 {@link AuthInterceptor} 负责校验 token 合法性。
 *
 * <p>token 载荷（payload）中携带三项信息：
 * <ul>
 *   <li>{@code account} —— 登录账号（管理员账号 或 用户邮箱）</li>
 *   <li>{@code role}    —— 角色：admin（管理员）/ user（前台用户）</li>
 *   <li>{@code uid}     —— 用户/管理员主键 ID</li>
 * </ul>
 *
 * @author 杨伟宏
 */
@Component
public class JwtTokenUtil {

    /** 签名秘钥，从配置文件读取 */
    @Value("${ebusiness.jwt.secret}")
    private String secret;

    /** token 有效期（秒） */
    @Value("${ebusiness.jwt.expiration}")
    private long expiration;

    /** 载荷 key：账号 */
    public static final String CLAIM_ACCOUNT = "account";
    /** 载荷 key：角色 */
    public static final String CLAIM_ROLE = "role";
    /** 载荷 key：主键 ID */
    public static final String CLAIM_UID = "uid";

    /** 角色值：管理员 */
    public static final String ROLE_ADMIN = "admin";
    /** 角色值：前台用户 */
    public static final String ROLE_USER = "user";

    /**
     * 签发 token
     *
     * @param account 登录账号（管理员账号 / 用户邮箱）
     * @param role    角色：{@link #ROLE_ADMIN} 或 {@link #ROLE_USER}
     * @param uid     用户 / 管理员主键 ID
     * @return JWT 字符串
     */
    public String createToken(String account, String role, Integer uid) {
        Map<String, Object> payload = new HashMap<>(8);
        payload.put(CLAIM_ACCOUNT, account);
        payload.put(CLAIM_ROLE, role);
        payload.put(CLAIM_UID, uid);
        // 签发时间
        payload.put("iat", DateUtil.date());
        // 过期时间
        payload.put("exp", DateUtil.offsetSecond(new Date(), (int) expiration));
        return JWTUtil.createToken(payload, secret.getBytes());
    }

    /**
     * 解析并校验 token
     *
     * @param token JWT 字符串
     * @return 解析后的 JWT 对象；token 非法或过期时返回 null
     */
    public JWT parseToken(String token) {
        if (StrUtil.isBlank(token)) {
            return null;
        }
        try {
            JWT jwt = JWTUtil.parseToken(token).setKey(secret.getBytes());
            // 校验签名
            if (!jwt.verify()) {
                return null;
            }
            // 校验是否过期
            JWTValidator.of(jwt).validateDate(DateUtil.date());
            return jwt;
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 从 token 中取出账号
     *
     * @return 账号；token 非法时返回 null
     */
    public String getAccount(String token) {
        JWT jwt = parseToken(token);
        return jwt == null ? null : (String) jwt.getPayload(CLAIM_ACCOUNT);
    }

    /**
     * 从 token 中取出角色
     *
     * @return admin / user；token 非法时返回 null
     */
    public String getRole(String token) {
        JWT jwt = parseToken(token);
        return jwt == null ? null : (String) jwt.getPayload(CLAIM_ROLE);
    }

    /**
     * 从 token 中取出用户 / 管理员主键 ID
     *
     * @return 主键 ID；token 非法时返回 null
     */
    public Integer getUid(String token) {
        JWT jwt = parseToken(token);
        if (jwt == null) {
            return null;
        }
        Object uid = jwt.getPayload(CLAIM_UID);
        return uid == null ? null : Integer.valueOf(uid.toString());
    }

    /**
     * 校验 token 是否有效
     *
     * @return true 表示有效且未过期
     */
    public boolean validate(String token) {
        return parseToken(token) != null;
    }

    /**
     * 从请求头中剥离 "Bearer " 前缀
     *
     * @param headerValue Authorization 请求头的原始值
     * @return 纯 token 字符串；格式不对时返回 null
     */
    public static String stripBearer(String headerValue) {
        if (StrUtil.isBlank(headerValue)) {
            return null;
        }
        if (headerValue.startsWith("Bearer ")) {
            return headerValue.substring(7).trim();
        }
        return headerValue.trim();
    }
}
