package com.ebusiness.controller.admin;

import com.ebusiness.common.util.ResponseResult;
import com.ebusiness.service.GoodsService;
import com.ebusiness.service.OrderBaseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 后台数据统计控制器
 *
 * <p>为 ECharts 图表提供数据，接口前缀：/api/admin/statistics
 *
 * @author 杨伟宏
 */
@RestController
@RequestMapping("/api/admin/statistics")
public class StatisticsController {

    @Autowired
    private GoodsService goodsService;

    @Autowired
    private OrderBaseService orderBaseService;

    /**
     * 后台首页概览（商品数 / 用户数 / 订单数 / 销售总额 / 待处理订单）
     *
     * <p>GET /api/admin/statistics/dashboard
     */
    @GetMapping("/dashboard")
    public ResponseResult<Object> dashboard() {
        return ResponseResult.success(goodsService.getDashboard().getData());
    }

    /**
     * 按类型统计销售情况（柱状图 / 折线图数据源）
     *
     * <p>GET /api/admin/statistics/salesByType
     */
    @GetMapping("/salesByType")
    public ResponseResult<Object> salesByType() {
        return ResponseResult.success(goodsService.getSalesByType().getData());
    }

    /**
     * 热销商品 TOP N
     *
     * <p>GET /api/admin/statistics/hotGoods?limit=10
     */
    @GetMapping("/hotGoods")
    public ResponseResult<Object> hotGoods(@RequestParam(defaultValue = "10") Integer limit) {
        return ResponseResult.success(goodsService.getHotGoods(limit).getData());
    }

    /**
     * 按自然月统计销售情况（月度折线/柱状图数据源）
     *
     * <p>GET /api/admin/statistics/salesByMonth?months=6
     * <p>返回最近 N 个月的数据，没有成交的月份补 0，X 轴保持连续
     */
    @GetMapping("/salesByMonth")
    public ResponseResult<Object> salesByMonth(@RequestParam(defaultValue = "6") Integer months) {
        return ResponseResult.success(goodsService.getSalesByMonth(months).getData());
    }

    /**
     * 订单状态分布（饼图 / 环形图数据源）
     *
     * <p>GET /api/admin/statistics/orderStatus
     */
    @GetMapping("/orderStatus")
    public ResponseResult<Object> orderStatus() {
        return ResponseResult.success(orderBaseService.getOrderCountByStatus().getData());
    }

    /**
     * 订单趋势（近 N 天下单量与金额）
     *
     * <p>GET /api/admin/statistics/orderTrend?days=7
     */
    @GetMapping("/orderTrend")
    public ResponseResult<Object> orderTrend(@RequestParam(defaultValue = "7") Integer days) {
        return ResponseResult.success(orderBaseService.getOrderTrend(days).getData());
    }
}
