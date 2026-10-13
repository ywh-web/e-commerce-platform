package com.ebusiness.controller.before;

import com.ebusiness.common.security.UserContext;
import com.ebusiness.common.util.ResponseResult;
import com.ebusiness.entity.BUser;
import com.ebusiness.service.BUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 前台用户控制器（注册 / 登录 / 个人中心）
 *
 * <p>接口前缀：/api/before
 *
 * @author 杨伟宏
 */
@RestController("beforeBUserController")
@RequestMapping("/api/before")
public class BUserController {

    @Autowired
    private BUserService bUserService;

    /**
     * 用户注册
     *
     * <p>POST /api/before/register
     * <p>请求体：{ "bemail": "a@b.com", "bpwd": "123456", "bname": "小样" }
     */
    @PostMapping("/register")
    public ResponseResult<Object> register(@RequestBody BUser bUser) {
        return bUserService.register(bUser);
    }

    /**
     * 用户登录
     *
     * <p>POST /api/before/login
     * <p>请求体：{ "bemail": "test@ebusiness.com", "bpwd": "123456" }
     */
    @PostMapping("/login")
    public ResponseResult<Object> login(@RequestBody BUser bUser) {
        return bUserService.login(bUser);
    }

    /**
     * 查询当前登录用户资料
     *
     * <p>GET /api/before/info
     * <p>用户 ID 从 token 里取，不信任前端传参
     */
    @GetMapping("/info")
    public ResponseResult<Object> info() {
        Integer uid = UserContext.getUid();
        return bUserService.getInfo(uid);
    }

    /**
     * 修改当前登录用户资料
     *
     * <p>POST /api/before/info/update
     * <p>请求体：{ "bname": "新昵称", "bphone": "138...", "baddr": "..." }
     */
    @PostMapping("/info/update")
    public ResponseResult<Object> updateInfo(@RequestBody BUser bUser) {
        // 强制使用当前登录用户 ID，防止越权修改他人资料
        bUser.setBid(UserContext.getUid());
        return bUserService.updateInfo(bUser);
    }

    /**
     * 修改密码
     *
     * <p>POST /api/before/password
     * <p>请求体：{ "oldPwd": "123456", "newPwd": "654321" }
     */
    @PostMapping("/password")
    public ResponseResult<Object> updatePassword(@RequestBody Map<String, String> params) {
        return bUserService.updatePassword(
                UserContext.getUid(), params.get("oldPwd"), params.get("newPwd"));
    }

    /**
     * 退出登录
     *
     * <p>POST /api/before/logout
     * <p>服务端无状态，token 由前端自行清除，这里只做语义化响应
     */
    @PostMapping("/logout")
    public ResponseResult<Object> logout() {
        return ResponseResult.success("已安全退出", null);
    }
}
