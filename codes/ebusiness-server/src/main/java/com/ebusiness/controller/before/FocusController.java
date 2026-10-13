package com.ebusiness.controller.before;

import com.ebusiness.common.security.UserContext;
import com.ebusiness.common.util.ResponseResult;
import com.ebusiness.entity.Focus;
import com.ebusiness.service.FocusService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 前台商品收藏控制器
 *
 * <p>接口前缀：/api/before/focus
 *
 * @author 杨伟宏
 */
@RestController
@RequestMapping("/api/before/focus")
public class FocusController {

    @Autowired
    private FocusService focusService;

    /**
     * 收藏 / 取消收藏（切换式）
     *
     * <p>POST /api/before/focus/toggle
     * <p>请求体：{ "goodstableId": 1 }
     */
    @PostMapping("/toggle")
    public ResponseResult<Object> toggle(@RequestBody Focus focus) {
        focus.setBusertableId(UserContext.getUid());
        return focusService.toggleFocus(focus);
    }

    /**
     * 我的收藏列表
     *
     * <p>GET /api/before/focus/list
     */
    @GetMapping("/list")
    public ResponseResult<Object> list() {
        return focusService.getFocusByUid(UserContext.getUid());
    }

    /**
     * 判断当前用户是否已收藏某商品
     *
     * <p>GET /api/before/focus/check?goodstableId=1
     */
    @GetMapping("/check")
    public ResponseResult<Object> check(@RequestParam Integer goodstableId) {
        return focusService.isFocused(UserContext.getUid(), goodstableId);
    }

    /**
     * 取消收藏
     *
     * <p>DELETE /api/before/focus/delete/{fid}
     */
    @DeleteMapping("/delete/{fid}")
    public ResponseResult<Object> delete(@PathVariable Integer fid) {
        return focusService.deleteFocus(fid);
    }
}
