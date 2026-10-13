package com.ebusiness.service.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ebusiness.common.util.ResponseResult;
import com.ebusiness.entity.Goods;
import com.ebusiness.entity.GoodsType;
import com.ebusiness.mapper.GoodsMapper;
import com.ebusiness.mapper.GoodsTypeMapper;
import com.ebusiness.service.GoodsTypeService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 商品类型业务实现
 *
 * @author 杨伟宏
 */
@Slf4j
@Service
public class GoodsTypeServiceImpl extends ServiceImpl<GoodsTypeMapper, GoodsType> implements GoodsTypeService {

    /** 后台类型管理列表每页条数 */
    private static final int ADMIN_PAGE_SIZE = 10;

    @Autowired
    private GoodsMapper goodsMapper;

    @Override
    public ResponseResult<List<GoodsType>> getAllTypes() {
        QueryWrapper<GoodsType> wrapper = new QueryWrapper<>();
        wrapper.orderByAsc("id");
        List<GoodsType> types = list(wrapper);

        // 为每个类型填充商品数量，后台列表展示更直观
        for (GoodsType type : types) {
            Long count = goodsMapper.selectCount(
                    new QueryWrapper<Goods>().eq("goodstype_id", type.getId()));
            type.setGoodsCount(count == null ? 0L : count);
        }
        return ResponseResult.success(types);
    }

    @Override
    public ResponseResult<Object> getTypesByPage(Integer currentPage, String keywords) {
        int page = (currentPage == null || currentPage < 1) ? 1 : currentPage;
        Page<GoodsType> pageParam = new Page<>(page, ADMIN_PAGE_SIZE);

        QueryWrapper<GoodsType> wrapper = new QueryWrapper<>();
        if (StrUtil.isNotBlank(keywords)) {
            wrapper.like("name", keywords.trim());
        }
        wrapper.orderByAsc("id");

        Page<GoodsType> result = page(pageParam, wrapper);

        // 为每条记录填充商品数量，列表里直接能看到"该类型下有几种商品"
        for (GoodsType type : result.getRecords()) {
            Long count = goodsMapper.selectCount(
                    new QueryWrapper<Goods>().eq("goodstype_id", type.getId()));
            type.setGoodsCount(count == null ? 0L : count);
        }

        Map<String, Object> data = new HashMap<>(4);
        data.put("typeList", result.getRecords());
        data.put("total", result.getTotal());
        data.put("totalPage", result.getPages());
        data.put("currentPage", page);
        return ResponseResult.success(data);
    }

    @Override
    public ResponseResult<Object> add(GoodsType goodsType) {
        if (goodsType == null || StrUtil.isBlank(goodsType.getName())) {
            return ResponseResult.fail("类型名称不能为空");
        }
        String name = goodsType.getName().trim();

        // 同名类型不允许重复添加
        QueryWrapper<GoodsType> wrapper = new QueryWrapper<>();
        wrapper.eq("name", name);
        if (count(wrapper) > 0) {
            return ResponseResult.fail("类型「" + name + "」已存在");
        }

        GoodsType entity = new GoodsType();
        entity.setName(name);
        if (save(entity)) {
            return ResponseResult.success("类型添加成功", null);
        }
        return ResponseResult.fail("类型添加失败");
    }

    @Override
    public ResponseResult<Object> update(GoodsType goodsType) {
        if (goodsType == null || goodsType.getId() == null || StrUtil.isBlank(goodsType.getName())) {
            return ResponseResult.fail("类型 ID 与名称不能为空");
        }
        String name = goodsType.getName().trim();

        // 排除自身后检查重名
        QueryWrapper<GoodsType> wrapper = new QueryWrapper<>();
        wrapper.eq("name", name).ne("id", goodsType.getId());
        if (count(wrapper) > 0) {
            return ResponseResult.fail("类型「" + name + "」已存在");
        }

        GoodsType entity = new GoodsType();
        entity.setId(goodsType.getId());
        entity.setName(name);
        if (updateById(entity)) {
            return ResponseResult.success("类型修改成功", null);
        }
        return ResponseResult.fail("类型修改失败");
    }

    @Override
    public ResponseResult<Object> delete(GoodsType goodsType) {
        if (goodsType == null || goodsType.getId() == null) {
            return ResponseResult.fail("类型 ID 不能为空");
        }
        // 该类型下有商品时不允许删除（外键约束也会拦，但提前给出友好提示）
        Long goodsCount = goodsMapper.selectCount(
                new QueryWrapper<Goods>().eq("goodstype_id", goodsType.getId()));
        if (goodsCount != null && goodsCount > 0) {
            return ResponseResult.fail("该类型下有 " + goodsCount + " 件商品，请先删除或转移商品");
        }
        if (removeById(goodsType.getId())) {
            return ResponseResult.success("类型删除成功", null);
        }
        return ResponseResult.fail("类型删除失败");
    }
}
