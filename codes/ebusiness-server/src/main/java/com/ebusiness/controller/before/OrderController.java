package com.ebusiness.controller.before;

import com.ebusiness.common.security.UserContext;
import com.ebusiness.common.util.ResponseResult;
import com.ebusiness.entity.OrderBase;
import com.ebusiness.service.OrderBaseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 前台订单控制器
 *
 * <p>接口前缀：/api/before/order
 *
 * @author 杨伟宏
 */
@RestController("beforeOrderController")
@RequestMapping("/api/before/order")
public class OrderController {

    @Autowired
    private OrderBaseService orderBaseService;

    /**
     * 提交订单
     *
     * <p>POST /api/before/order/submit
     * <p>请求体：{ "cartIds": [1, 2] }
     */
    @PostMapping("/submit")
    public ResponseResult<Object> submit(@RequestBody Map<String, Object> params) {
        OrderBase orderBase = new OrderBase();
        orderBase.setBusertableId(UserContext.getUid());

        // 取出前端提交的购物车记录 ID 列表
        Object rawIds = params.get("cartIds");
        List<Integer> cartIds = null;
        if (rawIds instanceof List) {
            cartIds = new java.util.ArrayList<>();
            for (Object o : (List<?>) rawIds) {
                cartIds.add(Integer.valueOf(o.toString()));
            }
        }
        return orderBaseService.submitOrder(orderBase, cartIds);
    }

    /**
     * 我的订单列表（分页）
     *
     * <p>GET /api/before/order/list?currentPage=1
     */
    @GetMapping("/list")
    public ResponseResult<Object> list(@RequestParam(defaultValue = "1") Integer currentPage) {
        return orderBaseService.getOrdersByUid(currentPage, UserContext.getUid());
    }

    /**
     * 订单详情（商品清单）
     *
     * <p>GET /api/before/order/detail/{id}
     *
     * <p>注意：必须带上当前登录用户，只允许查自己的订单明细。
     * 否则任何登录用户只要猜到订单 id 就能看到别人的收货信息与商品清单。
     */
    @GetMapping("/detail/{id}")
    public ResponseResult<Object> detail(@PathVariable Integer id) {
        OrderBase query = new OrderBase();
        query.setId(id);
        // 关键的归属限制：Mapper 里已支持按 busertable_id 过滤
        query.setBusertableId(UserContext.getUid());
        return ResponseResult.success(orderBaseService.getOrdersDetail(query).getData());
    }

    /**
     * 取消订单
     *
     * <p>POST /api/before/order/cancel
     * <p>请求体：{ "id": 1 }
     *
     * <p>注意：busertableId 一律以登录态为准，不信任请求体传的值，
     * 防止越权取消他人订单（Service 层还会再校验一次归属）。
     */
    @PostMapping("/cancel")
    public ResponseResult<Object> cancel(@RequestBody OrderBase orderBase) {
        if (orderBase != null) {
            orderBase.setBusertableId(UserContext.getUid());
        }
        return orderBaseService.cancelOrder(orderBase);
    }
}
