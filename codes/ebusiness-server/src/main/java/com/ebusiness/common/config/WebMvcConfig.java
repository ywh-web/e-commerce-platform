package com.ebusiness.common.config;

import com.ebusiness.common.security.AuthInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Web MVC 配置
 *
 * <p>两件事：
 * <ol>
 *   <li>注册登录鉴权拦截器 {@link AuthInterceptor}</li>
 *   <li>把磁盘上的商品图片上传目录映射为可访问的静态资源路径</li>
 * </ol>
 *
 * @author 杨伟宏
 */
@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    @Autowired
    private AuthInterceptor authInterceptor;

    /** 上传目录的磁盘绝对路径 */
    @Value("${ebusiness.upload.path}")
    private String uploadPath;

    /** 上传目录对应的访问前缀 */
    @Value("${ebusiness.upload.url-prefix}")
    private String uploadUrlPrefix;

    /**
     * 注册拦截器：默认拦截全部 /api/** 请求，白名单在拦截器内部判定
     */
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(authInterceptor)
                .addPathPatterns("/api/**")
                .excludePathPatterns(
                        "/error",
                        "/favicon.ico"
                );
    }

    /**
     * 静态资源映射：/upload/xxx.jpg → {磁盘路径}/xxx.jpg
     *
     * <p>这样前端上传商品图片后，用 {@code /eBusiness/upload/文件名} 就能访问到。
     */
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler(uploadUrlPrefix + "**")
                .addResourceLocations("file:" + uploadPath);
    }
}
