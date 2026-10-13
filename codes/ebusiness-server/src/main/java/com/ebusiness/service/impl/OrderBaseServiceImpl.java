package com.ebusiness.service.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ebusiness.common.config.GlobalExceptionHandler.BusinessException;
import com.ebusiness.common.util.ResponseResult;
import com.ebusiness.common.util.UploadUtil;
import com.ebusiness.entity.*;
import com.ebusiness.mapper.*;
import com.ebusiness.service.OrderBaseService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 订单业务实现
 *
 * <p>本类承载电商平台最核心的业务流程，重点看 {@link #submitOrder}。
 *
 * @author 杨伟宏
 */
@Slf4j
@Service
public class OrderBaseServiceImpl extends ServiceImpl<OrderBaseMapper, OrderBase> implements OrderBaseService {

    /** 每页条数 */
    private static final int PAGE_SIZE = 10;

    @Autowired
    private CartMapper cartMapper;

    @Autowired
    private OrderDetailMapper orderDetailMapper;

    @Autowired
    private GoodsMapper goodsMapper;

    @Autowired
    private BUserMapper bUserMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ResponseResult<Object> submitOrder(OrderBase orderBase, List<Integer> cartIds) {
        if (orderBase == null || orderBase.getBusertableId() == null) {
            return ResponseResult.fail("用户未登录，无法下单");
        }
        if (cartIds == null || cartIds.isEmpty()) {
            return ResponseResult.fail("请先选择要购买的商品");
        }

        Integer uid = orderBase.getBusertableId();

        // ---------- 第 1 步：取出选中的购物车记录，校验库存 ----------
        QueryWrapper<Cart> cartWrapper = new QueryWrapper<>();
        cartWrapper.eq("busertable_id", uid).in("cid", cartIds);
        List<Cart> carts = cartMapper.selectList(cartWrapper);
        if (carts.isEmpty()) {
            return ResponseResult.fail("购物车中未找到待下单商品");
        }

        List<OrderDetail> details = new ArrayList<>(carts.size());
        BigDecimal totalAmount = BigDecimal.ZERO;
        List<Goods> goodsToUpdate = new ArrayList<>(carts.size());

        for (Cart cart : carts) {
            Goods goods = goodsMapper.selectById(cart.getGoodstableId());
            if (goods == null) {
                throw new BusinessException("商品不存在或已被删除，请刷新购物车");
            }
            if (goods.getIsoff() != null && goods.getIsoff() == 1) {
                throw new BusinessException("商品「" + goods.getName() + "」已下架，无法下单");
            }
            if (goods.getStock() == null || goods.getStock() < cart.getShoppingnum()) {
                throw new BusinessException("商品「" + goods.getName() + "」库存不足，"
                        + "仅剩 " + (goods.getStock() == null ? 0 : goods.getStock()) + " 件");
            }
            // 暂无货：库存清零并标记下架
            if (goods.getStock().equals(cart.getShoppingnum())) {
                goods.setIsoff(1);
            }
            goods.setStock(goods.getStock() - cart.getShoppingnum());
            goods.setSales((goods.getSales() == null ? 0 : goods.getSales()) + cart.getShoppingnum());
            goodsToUpdate.add(goods);

            // 组装订单详情（名称、单价、规格都做快照，商品后续改价/改规格不影响历史订单）
            OrderDetail detail = new OrderDetail();
            detail.setGoodstableId(goods.getId());
            detail.setGoodsname(goods.getName());
            detail.setPrice(goods.getPrice());
            detail.setShoppingnum(cart.getShoppingnum());
            detail.setSpec(cart.getSpec() == null ? "" : cart.getSpec());
            details.add(detail);

            totalAmount = totalAmount.add(
                    goods.getPrice().multiply(BigDecimal.valueOf(cart.getShoppingnum())));
        }

        // ---------- 第 2 步：生成订单基础记录 ----------
        OrderBase order = new OrderBase();
        order.setBusertableId(uid);
        order.setOrdernumber(UploadUtil.generateOrderNumber());
        order.setAmount(totalAmount);
        order.setStatus(1); // 1-待付款
        order.setDatetime(LocalDateTime.now());
        if (!save(order)) {
            throw new BusinessException("订单创建失败，请重试");
        }

        // ---------- 第 3 步：批量写入订单详情 ----------
        for (OrderDetail detail : details) {
            detail.setOrderbaseId(order.getId());
            orderDetailMapper.insert(detail);
        }

        // ---------- 第 4 步：扣减库存、累加销量 ----------
        for (Goods goods : goodsToUpdate) {
            goodsMapper.updateById(goods);
        }

        // ---------- 第 5 步：清空已下单的购物车记录 ----------
        QueryWrapper<Cart> clearWrapper = new QueryWrapper<>();
        clearWrapper.eq("busertable_id", uid).in("cid", cartIds);
        cartMapper.delete(clearWrapper);

        log.info("用户 {} 下单成功，订单号 {}，金额 {}", uid, order.getOrdernumber(), totalAmount);

        Map<String, Object> data = new HashMap<>(4);
        data.put("orderId", order.getId());
        data.put("ordernumber", order.getOrdernumber());
        data.put("amount", totalAmount);
        return ResponseResult.success("下单成功", data);
    }

    @Override
    public ResponseResult<Object> getOrdersByUid(Integer currentPage, Integer busertableId) {
        if (busertableId == null) {
            return ResponseResult.fail("用户 ID 不能为空");
        }
        int page = (currentPage == null || currentPage < 1) ? 1 : currentPage;
        Page<OrderBase> pageParam = new Page<>(page, PAGE_SIZE);

        QueryWrapper<OrderBase> wrapper = new QueryWrapper<>();
        wrapper.eq("busertable_id", busertableId).orderByDesc("id");

        Page<OrderBase> result = page(pageParam, wrapper);

        Map<String, Object> data = new HashMap<>(4);
        data.put("orders", result.getRecords());
        data.put("totalPage", result.getPages());
        data.put("total", result.getTotal());
        data.put("currentPage", page);
        return ResponseResult.success(data);
    }

    @Override
    public ResponseResult<List<Map<String, Object>>> getOrdersDetail(OrderBase orderBase) {
        return ResponseResult.success(baseMapper.getOrdersDetail(orderBase));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ResponseResult<Object> cancelOrder(OrderBase orderBase) {
        if (orderBase == null || orderBase.getId() == null) {
            return ResponseResult.fail("订单 ID 不能为空");
        }
        OrderBase order = getById(orderBase.getId());
        if (order == null) {
            return ResponseResult.fail("订单不存在");
        }
        // 归属校验：只能操作自己的订单。
        // 订单不属于当前用户时统一回"订单不存在"，不暴露"存在但不属于你"，
        // 避免这个接口被用来探测订单 id 是否有效（越权/枚举）。
        if (orderBase.getBusertableId() == null
                || !orderBase.getBusertableId().equals(order.getBusertableId())) {
            return ResponseResult.fail("订单不存在");
        }
        // 只有待付款状态可以取消
        if (order.getStatus() != 1) {
            return ResponseResult.fail("当前订单状态不允许取消");
        }

        // 回滚库存与销量
        List<OrderDetail> details = orderDetailMapper.selectList(
                new QueryWrapper<OrderDetail>().eq("orderbase_id", order.getId()));
        for (OrderDetail detail : details) {
            Goods goods = goodsMapper.selectById(detail.getGoodstableId());
            if (goods == null) {
                continue;
            }
            UpdateWrapper<Goods> updateWrapper = new UpdateWrapper<>();
            updateWrapper.eq("id", goods.getId())
                    .setSql("stock = stock + " + detail.getShoppingnum())
                    .setSql("sales = CASE WHEN sales >= " + detail.getShoppingnum()
                            + " THEN sales - " + detail.getShoppingnum() + " ELSE 0 END")
                    .set("isoff", 0); // 回滚时重新上架
            goodsMapper.update(null, updateWrapper);
        }

        // 状态改为已取消
        UpdateWrapper<OrderBase> orderWrapper = new UpdateWrapper<>();
        orderWrapper.eq("id", order.getId()).set("status", 5);
        update(orderWrapper);

        log.info("订单 {} 已取消", order.getOrdernumber());
        return ResponseResult.success("订单已取消", null);
    }

    // ==================== 后台管理 ====================

    @Override
    public ResponseResult<Object> getAllOrders(Integer currentPage, OrderBase orderBase) {
        int page = (currentPage == null || currentPage < 1) ? 1 : currentPage;
        Page<OrderBase> pageParam = new Page<>(page, PAGE_SIZE);

        QueryWrapper<OrderBase> wrapper = new QueryWrapper<>();
        if (orderBase != null) {
            if (orderBase.getId() != null && orderBase.getId() > 0) {
                wrapper.eq("id", orderBase.getId());
            }
            if (StrUtil.isNotBlank(orderBase.getOrdernumber())) {
                wrapper.like("ordernumber", orderBase.getOrdernumber().trim());
            }
            if (orderBase.getStatus() != null && orderBase.getStatus() > 0) {
                wrapper.eq("status", orderBase.getStatus());
            }
        }
        wrapper.orderByDesc("id");

        Page<OrderBase> result = page(pageParam, wrapper);

        // 填充下单用户的邮箱与昵称
        List<OrderBase> orders = result.getRecords();
        for (OrderBase order : orders) {
            BUser user = bUserMapper.selectById(order.getBusertableId());
            if (user != null) {
                order.setBemail(user.getBemail());
                order.setBname(user.getBname());
            }
        }

        Map<String, Object> data = new HashMap<>(4);
        data.put("orders", orders);
        data.put("totalPage", result.getPages());
        data.put("total", result.getTotal());
        data.put("currentPage", page);
        return ResponseResult.success(data);
    }

    @Override
    public ResponseResult<Object> updateStatus(OrderBase orderBase) {
        if (orderBase == null || orderBase.getId() == null || orderBase.getStatus() == null) {
            return ResponseResult.fail("订单 ID 与状态不能为空");
        }
        if (orderBase.getStatus() < 1 || orderBase.getStatus() > 5) {
            return ResponseResult.fail("订单状态值不合法");
        }
        UpdateWrapper<OrderBase> wrapper = new UpdateWrapper<>();
        wrapper.eq("id", orderBase.getId()).set("status", orderBase.getStatus());
        if (update(wrapper)) {
            return ResponseResult.success("订单状态已更新", null);
        }
        return ResponseResult.fail("订单状态更新失败");
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ResponseResult<Object> deleteOrder(OrderBase orderBase) {
        if (orderBase == null || orderBase.getId() == null) {
            return ResponseResult.fail("订单 ID 不能为空");
        }
        // 先删详情，再删主表，避免外键报错
        orderDetailMapper.delete(new QueryWrapper<OrderDetail>().eq("orderbase_id", orderBase.getId()));
        if (removeById(orderBase.getId())) {
            return ResponseResult.success("订单删除成功", null);
        }
        return ResponseResult.fail("订单删除失败");
    }

    // ==================== 统计 ====================

    @Override
    public ResponseResult<List<Map<String, Object>>> getOrderCountByStatus() {
        return ResponseResult.success(baseMapper.getOrderCountByStatus());
    }

    @Override
    public ResponseResult<List<Map<String, Object>>> getOrderTrend(Integer days) {
        int size = (days == null || days < 1) ? 7 : days;

        // 近 N 天按日期分组统计：
        //   orderCount —— 当天下单量（排除"已取消"，取消的订单不算有效下单）
        //   amount     —— 当天"成交金额"（只累加 已付款/已发货/已完成 的有效订单），
        //                 与首页概览"销售总额"、类目销售统计保持同一口径。
        // 日期为空的日子由前端补 0。
        List<Map<String, Object>> raw = baseMapper.selectMaps(
                new QueryWrapper<OrderBase>()
                        .select("DATE_FORMAT(datetime, '%Y-%m-%d') AS date",
                                "COUNT(*) AS orderCount",
                                "IFNULL(SUM(CASE WHEN status IN (2, 3, 4) THEN amount ELSE 0 END), 0) AS amount")
                        .ge("datetime", LocalDateTime.now().minusDays(size))
                        .ne("status", 5)
                        .groupBy("DATE_FORMAT(datetime, '%Y-%m-%d')")
                        .orderByAsc("date"));
        return ResponseResult.success(raw);
    }
}
