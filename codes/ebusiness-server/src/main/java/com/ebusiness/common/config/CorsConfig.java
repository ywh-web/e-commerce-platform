package com.ebusiness.common.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

/**
 * 跨域配置
 *
 * <p>前后端分离架构下前端跑在 8080、后端跑在 8443，属于跨域。
 * <p>两种解决方式本项目都配了，双保险：
 * <ol>
 *   <li>开发环境用 {@code vue.config.js} 里的 devServer.proxy 代理（前端代码里不出现跨域）</li>
 *   <li>后端再配 CorsFilter，方便直接用 Postman 或其它端口的前端联调</li>
 * </ol>
 *
 * @author 杨伟宏
 */
@Configuration
public class CorsConfig {

    /**
     * 全局跨域过滤器
     */
    @Bean
    public CorsFilter corsFilter() {
        CorsConfiguration config = new CorsConfiguration();
        // 允许携带凭证（Cookie / Authorization 头）
        config.setAllowCredentials(true);
        // 允许的来源：用 pattern 写法兼容 Spring Boot 2.4+ 对 "*" 的限制
        config.addAllowedOriginPattern("*");
        // 允许的请求头与方法
        config.addAllowedHeader("*");
        config.addAllowedMethod("*");
        // 暴露给前端的响应头
        config.addExposedHeader("Authorization");
        // 预检请求缓存 1 小时
        config.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return new CorsFilter(source);
    }
}
