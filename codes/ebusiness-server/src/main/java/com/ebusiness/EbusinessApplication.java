package com.ebusiness;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

/**
 * 电子商务平台后端服务启动类
 *
 * <p>课程项目：《前端开发技术课程设计》—— 电子商务平台前端系统设计与实现
 * <p>技术栈：Spring Boot + MyBatis-Plus + MySQL 8 + Redis + JWT
 *
 * <p>启动后服务地址：http://localhost:8443/eBusiness
 * <p>前端开发服务器（8080）通过 vue.config.js 里的代理把 /api 转发到这里。
 *
 * @author 杨伟宏
 */
@SpringBootApplication
// 扫描 Mapper 接口，省去在每个 Mapper 上写 @Mapper 注解
@MapperScan("com.ebusiness.mapper")
// 开启 Spring Cache 注解缓存（商品列表等读多写少的数据）
@EnableCaching
public class EbusinessApplication {

    public static void main(String[] args) {
        SpringApplication.run(EbusinessApplication.class, args);
        System.out.println("\n========================================");
        System.out.println("  电子商务平台后端启动成功！");
        System.out.println("  接口根路径：http://localhost:8443/eBusiness");
        System.out.println("========================================\n");
    }
}
