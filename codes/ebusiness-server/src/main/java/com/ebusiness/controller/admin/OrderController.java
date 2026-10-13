package com.ebusiness.controller.admin;

import com.ebusiness.common.util.ResponseResult;
import com.ebusiness.entity.OrderBase;
import com.ebusiness.service.OrderBaseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 后台订单管理控制器
 *
 * <p>接口前缀：/api/admin/order
 *
 * @author 杨伟宏
 */
@RestController("adminOrderController")
@RequestMapping("/api/admin/order")
public class OrderController {

    @Autowired
    private OrderBaseService orderBaseService;

    /**
     * 订单列表（分页 + 订单号搜索 + 状态筛选）
     *
     * <p>GET /api/admin/order/list?currentPage=1&ordernumber=2025&status=1
     */
    @GetMapping("/list")
    public ResponseResult<Object> list(@RequestParam(defaultValue = "1") Integer currentPage,
                                       @RequestParam(required = false) String ordernumber,
                                       @RequestParam(required = false) Integer status) {
        OrderBase query = new OrderBase();
        query.setOrdernumber(ordernumber);
        query.setStatus(status);
        return orderBaseService.getAllOrders(currentPage, query);
    }

    /**
     * 订单明细（展开行 / 详情页）
     *
     * <p>GET /api/admin/order/detail/{id}
     */
    @GetMapping("/detail/{id}")
    public ResponseResult<Object> detail(@PathVariable Integer id) {
        OrderBase query = new OrderBase();
        query.setId(id);
        return ResponseResult.success(orderBaseService.getOrdersDetail(query).getData());
    }

    /**
     * 修改订单状态（发货 / 完成 / 取消）
     *
     * <p>POST /api/admin/order/status
     * <p>请求体：{ "id": 1, "status": 3 }
     */
    @PostMapping("/status")
    public ResponseResult<Object> changeStatus(@RequestBody OrderBase orderBase) {
        return orderBaseService.updateStatus(orderBase);
    }

    /**
     * 删除订单
     *
     * <p>DELETE /api/admin/order/delete/{id}
     */
    @DeleteMapping("/delete/{id}")
    public ResponseResult<Object> delete(@PathVariable Integer id) {
        OrderBase orderBase = new OrderBase();
        orderBase.setId(id);
        return orderBaseService.deleteOrder(orderBase);
    }
}
