package com.ebusiness.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.ebusiness.entity.OrderBase;

import java.util.List;
import java.util.Map;

/**
 * 订单基础 Mapper
 *
 * <p>补充与订单详情联表的自定义查询。
 *
 * @author 杨伟宏
 */
public interface OrderBaseMapper extends BaseMapper<OrderBase> {

    /**
     * 查询订单明细（联表 orderdetail）
     *
     * <p>用于后台订单管理的展开行明细，以及用户订单详情的商品清单。
     *
     * @param orderBase 仅使用其 id（订单 ID）或 busertableId（用户 ID）作为查询条件
     * @return 明细列表，每行包含商品名称、单价、数量、图片
     */
    List<Map<String, Object>> getOrdersDetail(OrderBase orderBase);

    /**
     * 按订单状态统计订单数量 —— 供后台"订单统计"饼图使用
     *
     * @return 每行包含 status（状态码）、statusName（状态名称）、orderCount（订单数）
     */
    List<Map<String, Object>> getOrderCountByStatus();
}
