package com.ebusiness.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ebusiness.common.util.ResponseResult;
import com.ebusiness.entity.Focus;
import com.ebusiness.entity.Goods;
import com.ebusiness.mapper.FocusMapper;
import com.ebusiness.mapper.GoodsMapper;
import com.ebusiness.service.FocusService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 商品收藏业务实现
 *
 * @author 杨伟宏
 */
@Slf4j
@Service
public class FocusServiceImpl extends ServiceImpl<FocusMapper, Focus> implements FocusService {

    @Autowired
    private GoodsMapper goodsMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ResponseResult<Object> toggleFocus(Focus focus) {
        if (focus == null || focus.getBusertableId() == null || focus.getGoodstableId() == null) {
            return ResponseResult.fail("用户 ID 与商品 ID 不能为空");
        }
        // 商品存在性校验
        Goods goods = goodsMapper.selectById(focus.getGoodstableId());
        if (goods == null) {
            return ResponseResult.fail("商品不存在");
        }

        QueryWrapper<Focus> wrapper = new QueryWrapper<>();
        wrapper.eq("busertable_id", focus.getBusertableId())
                .eq("goodstable_id", focus.getGoodstableId());
        Focus exist = getOne(wrapper);

        Map<String, Object> data = new HashMap<>(2);
        boolean focused;
        String msg;

        if (exist != null) {
            // 已收藏 → 取消
            removeById(exist.getFid());
            focused = false;
            msg = "已取消收藏";
        } else {
            // 未收藏 → 新增
            Focus entity = new Focus();
            entity.setBusertableId(focus.getBusertableId());
            entity.setGoodstableId(focus.getGoodstableId());
            save(entity);
            focused = true;
            msg = "收藏成功";
        }

        data.put("focused", focused);
        return ResponseResult.success(msg, data);
    }

    @Override
    public ResponseResult<Object> getFocusByUid(Integer busertableId) {
        if (busertableId == null) {
            return ResponseResult.fail("用户 ID 不能为空");
        }
        QueryWrapper<Focus> wrapper = new QueryWrapper<>();
        wrapper.eq("busertable_id", busertableId).orderByDesc("fid");
        List<Focus> focuses = list(wrapper);

        // 联表填充商品信息
        for (Focus focus : focuses) {
            Goods goods = goodsMapper.selectById(focus.getGoodstableId());
            if (goods == null) {
                continue;
            }
            focus.setGoodsName(goods.getName());
            focus.setPrice(goods.getPrice());
            focus.setPicture(goods.getPicture());
            focus.setStock(goods.getStock());
            focus.setIntroduce(goods.getIntroduce());
        }

        Map<String, Object> data = new HashMap<>(2);
        data.put("focusList", focuses);
        data.put("total", focuses.size());
        return ResponseResult.success(data);
    }

    @Override
    public ResponseResult<Object> isFocused(Integer busertableId, Integer goodstableId) {
        Map<String, Object> data = new HashMap<>(2);
        boolean focused = false;
        if (busertableId != null && goodstableId != null) {
            QueryWrapper<Focus> wrapper = new QueryWrapper<>();
            wrapper.eq("busertable_id", busertableId)
                    .eq("goodstable_id", goodstableId);
            focused = count(wrapper) > 0;
        }
        data.put("focused", focused);
        return ResponseResult.success(data);
    }

    @Override
    public ResponseResult<Object> deleteFocus(Integer fid) {
        if (fid == null) {
            return ResponseResult.fail("收藏记录 ID 不能为空");
        }
        if (removeById(fid)) {
            return ResponseResult.success("已取消收藏", null);
        }
        return ResponseResult.fail("取消收藏失败");
    }
}
