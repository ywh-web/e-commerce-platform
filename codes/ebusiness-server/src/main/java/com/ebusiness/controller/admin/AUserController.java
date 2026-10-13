package com.ebusiness.controller.admin;

import com.ebusiness.common.util.ResponseResult;
import com.ebusiness.entity.AUser;
import com.ebusiness.service.AUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 后台管理员控制器
 *
 * <p>接口前缀：/api/admin/au
 *
 * @author 杨伟宏
 */
@RestController
@RequestMapping("/api/admin/au")
public class AUserController {

    @Autowired
    private AUserService aUserService;

    /**
     * 管理员登录
     *
     * <p>POST /api/admin/au/login
     * <p>请求体：{ "aname": "admin", "apwd": "123456" }
     */
    @PostMapping("/login")
    public ResponseResult<Object> login(@RequestBody AUser aUser) {
        return aUserService.login(aUser);
    }

    /**
     * 获取当前登录管理员信息
     *
     * <p>GET /api/admin/au/info
     */
    @GetMapping("/info")
    public ResponseResult<Object> info() {
        AUser aUser = aUserService.getOne(null);
        if (aUser != null) {
            aUser.setApwd(null);
        }
        return ResponseResult.success(aUser);
    }
}
