package com.ebusiness.common.security;

import cn.hutool.json.JSONUtil;
import com.ebusiness.common.util.ResponseResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.PrintWriter;

/**
 * 登录鉴权拦截器
 *
 * <p>职责：
 * <ol>
 *   <li>放行登录、注册、验证码、静态资源等公开接口</li>
 *   <li>校验请求头中的 JWT token，非法则返回 401</li>
 *   <li>校验通过后把登录信息写入 {@link UserContext}</li>
 *   <li>后台接口额外校验角色必须为 admin</li>
 * </ol>
 *
 * <p>注意：这是后端的最后一道防线。前端路由守卫只是"体验兜底"，
 * 真正决定能不能访问数据的是本拦截器。
 *
 * @author 杨伟宏
 */
@Component
public class AuthInterceptor implements HandlerInterceptor {

    @Autowired
    private JwtTokenUtil jwtTokenUtil;

    /** 无需登录即可访问的路径（前缀匹配，已去掉 context-path） */
    private static final String[] WHITE_LIST = {
            "/api/admin/au/login",       // 管理员登录
            "/api/before/login",         // 用户登录
            "/api/before/register",      // 用户注册
            "/api/common/captcha",       // 图形验证码
            "/api/before/goods/list",    // 前台商品列表（游客可浏览）
            "/api/before/goods/detail",  // 前台商品详情（游客可浏览）
            "/api/before/goods/types",   // 商品类型（游客可浏览）
            "/api/before/goods/recommend", // 首页推荐商品（游客可浏览）
            "/upload/",                  // 上传的图片静态资源
            "/error"
    };

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // 跨域预检请求直接放行
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            return true;
        }

        String uri = request.getRequestURI();
        // 去掉 context-path（/eBusiness），只保留业务路径
        String contextPath = request.getContextPath();
        if (contextPath != null && !contextPath.isEmpty() && uri.startsWith(contextPath)) {
            uri = uri.substring(contextPath.length());
        }

        // 白名单放行
        for (String white : WHITE_LIST) {
            if (uri.startsWith(white)) {
                return true;
            }
        }

        // 取出 token
        String header = request.getHeader("Authorization");
        String token = JwtTokenUtil.stripBearer(header);

        if (!jwtTokenUtil.validate(token)) {
            writeUnauthorized(response, "登录已过期，请重新登录");
            return false;
        }

        String role = jwtTokenUtil.getRole(token);
        Integer uid = jwtTokenUtil.getUid(token);
        String account = jwtTokenUtil.getAccount(token);

        // 后台接口必须管理员角色
        if (uri.startsWith("/api/admin/") && !JwtTokenUtil.ROLE_ADMIN.equals(role)) {
            writeForbidden(response, "无管理员权限，禁止访问");
            return false;
        }

        // 写入当前登录上下文
        UserContext.set(new UserContext.LoginUser(account, role, uid));
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response,
                                Object handler, Exception ex) {
        // 请求结束清理 ThreadLocal，防止线程复用串数据
        UserContext.clear();
    }

    /**
     * 输出 401 未登录响应（JSON 格式，与统一响应结构保持一致）
     *
     * <p>HTTP 状态码与响应体 {@code code} 字段保持同为 401。
     * 这样前端 Axios 无论走"失败分支（读 HTTP 状态）"还是"成功分支（读业务码）"
     * 都能识别登录失效并清 token、跳登录页，不会出现"只弹提示、卡在原页面"的情况。
     */
    private void writeUnauthorized(HttpServletResponse response, String msg) throws Exception {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json;charset=UTF-8");
        ResponseResult<Object> result = ResponseResult.unauthorized(msg);
        try (PrintWriter writer = response.getWriter()) {
            writer.write(JSONUtil.toJsonStr(result));
            writer.flush();
        }
    }

    /**
     * 输出 403 无权限响应
     *
     * <p>HTTP 状态码与响应体 {@code code} 字段同为 403，理由同 {@link #writeUnauthorized}。
     */
    private void writeForbidden(HttpServletResponse response, String msg) throws Exception {
        response.setStatus(HttpServletResponse.SC_FORBIDDEN);
        response.setContentType("application/json;charset=UTF-8");
        ResponseResult<Object> result = ResponseResult.forbidden(msg);
        try (PrintWriter writer = response.getWriter()) {
            writer.write(JSONUtil.toJsonStr(result));
            writer.flush();
        }
    }
}
