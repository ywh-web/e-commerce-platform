package com.ebusiness.controller.admin;

import com.ebusiness.common.util.ResponseResult;
import com.ebusiness.entity.BUser;
import com.ebusiness.service.BUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 后台用户管理控制器
 *
 * <p>接口前缀：/api/admin/user
 *
 * @author 杨伟宏
 */
@RestController("adminBUserController")
@RequestMapping("/api/admin/user")
public class BUserController {

    @Autowired
    private BUserService bUserService;

    /**
     * 用户列表（分页 + 邮箱/昵称模糊搜索）
     *
     * <p>GET /api/admin/user/list?currentPage=1&bemail=test
     */
    @GetMapping("/list")
    public ResponseResult<Object> list(@RequestParam(defaultValue = "1") Integer currentPage,
                                       @RequestParam(required = false) String bemail,
                                       @RequestParam(required = false) String bname) {
        BUser query = new BUser();
        query.setBemail(bemail);
        query.setBname(bname);
        return bUserService.getAllUsers(currentPage, query);
    }

    /**
     * 删除用户（有订单时不允许删除）
     *
     * <p>DELETE /api/admin/user/delete/{bid}
     */
    @DeleteMapping("/delete/{bid}")
    public ResponseResult<Object> delete(@PathVariable Integer bid) {
        BUser bUser = new BUser();
        bUser.setBid(bid);
        return bUserService.delete(bUser);
    }
}
