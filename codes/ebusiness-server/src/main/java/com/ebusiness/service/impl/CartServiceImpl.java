package com.ebusiness.service.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ebusiness.common.util.ResponseResult;
import com.ebusiness.entity.Cart;
import com.ebusiness.entity.Goods;
import com.ebusiness.mapper.CartMapper;
import com.ebusiness.mapper.GoodsMapper;
import com.ebusiness.service.CartService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 购物车业务实现
 *
 * @author 杨伟宏
 */
@Slf4j
@Service
public class CartServiceImpl extends ServiceImpl<CartMapper, Cart> implements CartService {

    @Autowired
    private GoodsMapper goodsMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ResponseResult<Object> addCart(Cart cart) {
        if (cart == null || cart.getBusertableId() == null || cart.getGoodstableId() == null) {
            return ResponseResult.fail("用户 ID 与商品 ID 不能为空");
        }
        int num = (cart.getShoppingnum() == null || cart.getShoppingnum() < 1) ? 1 : cart.getShoppingnum();
        // 规格统一规整为字符串（空串代表无规格），
        // 这样"同一用户 + 同一商品 + 同一规格"才能用等值条件精确匹配
        String spec = cart.getSpec() == null ? "" : cart.getSpec().trim();

        // 校验商品是否存在、是否上架、库存是否充足
        Goods goods = goodsMapper.selectById(cart.getGoodstableId());
        if (goods == null) {
            return ResponseResult.fail("商品不存在");
        }
        if (goods.getIsoff() != null && goods.getIsoff() == 1) {
            return ResponseResult.fail("商品已下架，无法加入购物车");
        }
        // 该商品定义了规格，就必须先选规格才能加购，
        // 否则购物车里会出现"没写规格"的记录，下单后也无从追溯
        if (StrUtil.isNotBlank(goods.getSpecs()) && StrUtil.isBlank(spec)) {
            return ResponseResult.fail("请先选择商品规格");
        }

        // 查询购物车中是否已有该商品 + 该规格
        QueryWrapper<Cart> wrapper = new QueryWrapper<>();
        wrapper.eq("busertable_id", cart.getBusertableId())
                .eq("goodstable_id", cart.getGoodstableId())
                .eq("spec", spec);
        Cart exist = getOne(wrapper);

        if (exist != null) {
            // 已存在：累加数量
            int newNum = exist.getShoppingnum() + num;
            if (goods.getStock() != null && newNum > goods.getStock()) {
                return ResponseResult.fail("库存不足，当前仅剩 " + goods.getStock() + " 件");
            }
            UpdateWrapper<Cart> updateWrapper = new UpdateWrapper<>();
            updateWrapper.eq("cid", exist.getCid())
                    .set("shoppingnum", newNum);
            if (update(updateWrapper)) {
                return ResponseResult.success("已加入购物车", null);
            }
            return ResponseResult.fail("加入购物车失败");
        }

        // 不存在：新增一条
        if (goods.getStock() != null && num > goods.getStock()) {
            return ResponseResult.fail("库存不足，当前仅剩 " + goods.getStock() + " 件");
        }
        Cart entity = new Cart();
        entity.setBusertableId(cart.getBusertableId());
        entity.setGoodstableId(cart.getGoodstableId());
        entity.setShoppingnum(num);
        entity.setSpec(spec);
        if (save(entity)) {
            return ResponseResult.success("已加入购物车", null);
        }
        return ResponseResult.fail("加入购物车失败");
    }

    @Override
    public ResponseResult<Object> getCartByUid(Integer busertableId) {
        if (busertableId == null) {
            return ResponseResult.fail("用户 ID 不能为空");
        }
        QueryWrapper<Cart> wrapper = new QueryWrapper<>();
        wrapper.eq("busertable_id", busertableId).orderByDesc("cid");
        List<Cart> carts = list(wrapper);

        // 联表填充商品信息与小计
        BigDecimal totalAmount = BigDecimal.ZERO;
        int totalNum = 0;
        for (Cart cart : carts) {
            Goods goods = goodsMapper.selectById(cart.getGoodstableId());
            if (goods == null) {
                continue;
            }
            cart.setGoodsName(goods.getName());
            cart.setPrice(goods.getPrice());
            cart.setPicture(goods.getPicture());
            BigDecimal subtotal = goods.getPrice()
                    .multiply(BigDecimal.valueOf(cart.getShoppingnum()));
            cart.setSubtotal(subtotal);
            totalAmount = totalAmount.add(subtotal);
            totalNum += cart.getShoppingnum();
        }

        Map<String, Object> data = new HashMap<>(8);
        data.put("carts", carts);
        data.put("totalAmount", totalAmount);
        data.put("totalNum", totalNum);
        return ResponseResult.success(data);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ResponseResult<Object> updateCart(Cart cart) {
        if (cart == null || cart.getBcid() == null || cart.getBshoppingnum() == null) {
            return ResponseResult.fail("参数不完整");
        }
        List<Integer> cids = cart.getBcid();
        List<Integer> nums = cart.getBshoppingnum();
        if (cids.size() != nums.size()) {
            return ResponseResult.fail("参数长度不一致");
        }

        List<Cart> updateList = new ArrayList<>(cids.size());
        for (int i = 0; i < cids.size(); i++) {
            Integer cid = cids.get(i);
            Integer num = nums.get(i);
            if (cid == null || num == null || num < 1) {
                continue;
            }
            // 校验库存
            Cart dbCart = getById(cid);
            if (dbCart == null) {
                continue;
            }
            Goods goods = goodsMapper.selectById(dbCart.getGoodstableId());
            if (goods != null && goods.getStock() != null && num > goods.getStock()) {
                return ResponseResult.fail("「" + goods.getName() + "」库存不足，仅剩 "
                        + goods.getStock() + " 件");
            }
            Cart entity = new Cart();
            entity.setCid(cid);
            entity.setShoppingnum(num);
            updateList.add(entity);
        }

        if (!updateList.isEmpty()) {
            updateBatchById(updateList);
        }
        return ResponseResult.success("购物车已更新", null);
    }

    @Override
    public ResponseResult<Object> deleteCart(Integer cid) {
        if (cid == null) {
            return ResponseResult.fail("购物车记录 ID 不能为空");
        }
        if (removeById(cid)) {
            return ResponseResult.success("已从购物车移除", null);
        }
        return ResponseResult.fail("移除失败");
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ResponseResult<Object> clearCart(Cart cart) {
        if (cart == null || cart.getBusertableId() == null) {
            return ResponseResult.fail("用户 ID 不能为空");
        }
        QueryWrapper<Cart> wrapper = new QueryWrapper<>();
        wrapper.eq("busertable_id", cart.getBusertableId());
        remove(wrapper);
        return ResponseResult.success("购物车已清空", null);
    }

    @Override
    public ResponseResult<Integer> getCartCount(Integer busertableId) {
        if (busertableId == null) {
            return ResponseResult.success(0);
        }
        QueryWrapper<Cart> wrapper = new QueryWrapper<>();
        wrapper.eq("busertable_id", busertableId).select("IFNULL(SUM(shoppingnum), 0) AS total");
        List<Object> objs = baseMapper.selectObjs(wrapper);
        int total = 0;
        if (objs != null && !objs.isEmpty() && objs.get(0) != null) {
            total = Integer.parseInt(objs.get(0).toString());
        }
        return ResponseResult.success(total);
    }
}
