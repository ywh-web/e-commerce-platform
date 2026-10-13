package com.ebusiness.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.ebusiness.common.util.ResponseResult;
import com.ebusiness.entity.OrderBase;

import java.util.List;
import java.util.Map;

/**
 * 订单业务接口
 *
 * @author 杨伟宏
 */
public interface OrderBaseService extends IService<OrderBase> {

    // ==================== 前台下单 ====================

    /**
     * 提交订单（核心业务流程）
     *
     * <p>完整事务流程：
     * <ol>
     *   <li>校验购物车商品是否存在、库存是否充足</li>
     *   <li>生成订单基础记录（订单号 + 总金额）</li>
     *   <li>循环写入订单详情（商品名称与单价做快照）</li>
     *   <li>扣减商品库存、累加商品销量</li>
     *   <li>清空该用户的购物车</li>
     * </ol>
     * 以上任一步失败，整体回滚。
     *
     * @param orderBase 含 busertableId，以及待下单的 cid 列表（放在 cartIds 中）
     * @return data 中携带生成的订单号
     */
    ResponseResult<Object> submitOrder(OrderBase orderBase, List<Integer> cartIds);

    /**
     * 查询某用户的订单列表（分页）
     *
     * @param currentPage  页码
     * @param busertableId 用户 ID
     * @return data 含 orders 数组与 totalPage
     */
    ResponseResult<Object> getOrdersByUid(Integer currentPage, Integer busertableId);

    /**
     * 查询订单详情（商品清单）
     *
     * @param orderBase 含 id（订单 ID）或 busertableId
     * @return 明细行列表
     */
    ResponseResult<List<Map<String, Object>>> getOrdersDetail(OrderBase orderBase);

    /**
     * 用户取消订单
     *
     * <p>仅"待付款"状态可取消；取消后回滚商品库存与销量。
     *
     * @param orderBase 含 id
     * @return 操作结果
     */
    ResponseResult<Object> cancelOrder(OrderBase orderBase);

    // ==================== 后台管理 ====================

    /**
     * 后台订单列表（分页 + 订单号搜索）
     *
     * @param currentPage 页码
     * @param orderBase   筛选条件：id（精确）、ordernumber（模糊）
     * @return data 含 orders 数组与 totalPage
     */
    ResponseResult<Object> getAllOrders(Integer currentPage, OrderBase orderBase);

    /**
     * 后台修改订单状态（发货 / 完成 / 取消）
     *
     * @param orderBase 含 id 与 status
     * @return 操作结果
     */
    ResponseResult<Object> updateStatus(OrderBase orderBase);

    /**
     * 后台删除订单（连带删除订单详情）
     *
     * @param orderBase 含 id
     * @return 操作结果
     */
    ResponseResult<Object> deleteOrder(OrderBase orderBase);

    // ==================== 统计 ====================

    /**
     * 按订单状态统计订单数量（后台订单统计饼图）
     */
    ResponseResult<List<Map<String, Object>>> getOrderCountByStatus();

    /**
     * 订单趋势统计：近 N 天每天的下单量与金额
     *
     * @param days 统计天数
     * @return 每行含 date、orderCount、amount
     */
    ResponseResult<List<Map<String, Object>>> getOrderTrend(Integer days);
}
