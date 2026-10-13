package com.ebusiness.service.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ebusiness.common.util.ResponseResult;
import com.ebusiness.common.util.UploadUtil;
import com.ebusiness.entity.*;
import com.ebusiness.mapper.*;
import com.ebusiness.service.GoodsService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 商品业务实现
 *
 * @author 杨伟宏
 */
@Slf4j
@Service
public class GoodsServiceImpl extends ServiceImpl<GoodsMapper, Goods> implements GoodsService {

    /** 后台列表每页条数 */
    private static final int ADMIN_PAGE_SIZE = 10;
    /** 前台列表每页条数 */
    private static final int FRONT_PAGE_SIZE = 8;

    @Autowired
    private GoodsTypeMapper goodsTypeMapper;

    @Autowired
    private CartMapper cartMapper;

    @Autowired
    private FocusMapper focusMapper;

    @Autowired
    private OrderDetailMapper orderDetailMapper;

    @Autowired
    private OrderBaseMapper orderBaseMapper;

    @Autowired
    private BUserMapper bUserMapper;

    /** 商品图片上传的磁盘目录 */
    @Value("${ebusiness.upload.path}")
    private String uploadPath;

    // ==================== 后台管理 ====================

    @Override
    public ResponseResult<Object> getAllGoods(Integer currentPage, Goods goods) {
        int page = (currentPage == null || currentPage < 1) ? 1 : currentPage;
        Page<Goods> pageParam = new Page<>(page, ADMIN_PAGE_SIZE);

        QueryWrapper<Goods> wrapper = new QueryWrapper<>();
        if (goods != null) {
            if (goods.getGoodstypeId() != null && goods.getGoodstypeId() > 0) {
                wrapper.eq("goodstype_id", goods.getGoodstypeId());
            }
            if (StrUtil.isNotBlank(goods.getKeywords())) {
                wrapper.like("name", goods.getKeywords().trim());
            }
        }
        wrapper.orderByDesc("id");

        Page<Goods> result = page(pageParam, wrapper);
        fillTypeName(result.getRecords());

        Map<String, Object> data = new HashMap<>(4);
        data.put("goodsList", result.getRecords());
        data.put("totalPage", result.getPages());
        data.put("total", result.getTotal());
        data.put("currentPage", page);
        return ResponseResult.success(data);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ResponseResult<Object> addOrUpdate(Goods goods) {
        if (goods == null || StrUtil.isBlank(goods.getName())) {
            return ResponseResult.fail("商品名称不能为空");
        }
        if (goods.getPrice() == null || goods.getPrice().doubleValue() < 0) {
            return ResponseResult.fail("商品单价不能为空且不能为负数");
        }
        if (goods.getGoodstypeId() == null) {
            return ResponseResult.fail("请选择商品类型");
        }
        if (goods.getStock() == null || goods.getStock() < 0) {
            return ResponseResult.fail("库存数量不能为空且不能为负数");
        }

        // 1. 处理图片上传
        if (goods.getLogoFile() != null && !goods.getLogoFile().isEmpty()) {
            try {
                String newName = UploadUtil.savePicture(goods.getLogoFile(), uploadPath);
                if (newName != null) {
                    // 只存文件名，前端用 /eBusiness/upload/文件名 访问
                    goods.setPicture(newName);
                }
            } catch (Exception e) {
                log.error("商品图片上传失败", e);
                return ResponseResult.fail("图片上传失败：" + e.getMessage());
            }
        }

        // 2. 区分新增 / 修改
        boolean isAdd = "add".equalsIgnoreCase(goods.getAct());
        boolean ok;
        if (isAdd) {
            goods.setId(null);
            if (goods.getSales() == null) {
                goods.setSales(0);
            }
            if (goods.getIsoff() == null) {
                goods.setIsoff(0);
            }
            ok = save(goods);
        } else {
            if (goods.getId() == null) {
                return ResponseResult.fail("商品 ID 不能为空");
            }
            // 未重新上传图片时保留原图片
            if (StrUtil.isBlank(goods.getPicture())) {
                Goods db = getById(goods.getId());
                if (db != null) {
                    goods.setPicture(db.getPicture());
                }
            }
            ok = updateById(goods);
        }

        if (ok) {
            return ResponseResult.success(isAdd ? "商品添加成功" : "商品修改成功", null);
        }
        return ResponseResult.fail(isAdd ? "商品添加失败" : "商品修改失败");
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ResponseResult<Object> delete(Goods goods) {
        if (goods == null || goods.getId() == null) {
            return ResponseResult.fail("商品 ID 不能为空");
        }
        Integer gid = goods.getId();

        // 商品一旦被加购 / 收藏 / 下单，就不允许物理删除，否则外键会报错
        Long cartCount = cartMapper.selectCount(new QueryWrapper<Cart>().eq("goodstable_id", gid));
        if (cartCount != null && cartCount > 0) {
            return ResponseResult.fail("该商品已被加入购物车，不允许删除，可改为下架");
        }
        Long focusCount = focusMapper.selectCount(new QueryWrapper<Focus>().eq("goodstable_id", gid));
        if (focusCount != null && focusCount > 0) {
            return ResponseResult.fail("该商品已被收藏，不允许删除，可改为下架");
        }
        Long orderCount = orderDetailMapper.selectCount(new QueryWrapper<OrderDetail>().eq("goodstable_id", gid));
        if (orderCount != null && orderCount > 0) {
            return ResponseResult.fail("该商品已有成交记录，不允许删除，可改为下架");
        }

        if (removeById(gid)) {
            return ResponseResult.success("商品删除成功", null);
        }
        return ResponseResult.fail("商品删除失败");
    }

    @Override
    public ResponseResult<Object> changeStatus(Goods goods) {
        if (goods == null || goods.getId() == null || goods.getIsoff() == null) {
            return ResponseResult.fail("商品 ID 与状态不能为空");
        }
        Goods update = new Goods();
        update.setId(goods.getId());
        update.setIsoff(goods.getIsoff());
        if (updateById(update)) {
            return ResponseResult.success(goods.getIsoff() == 0 ? "商品已上架" : "商品已下架", null);
        }
        return ResponseResult.fail("状态修改失败");
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ResponseResult<Object> batchChangeStatus(Goods goods) {
        if (goods == null || goods.getIds() == null || goods.getIds().isEmpty()
                || goods.getIsoff() == null) {
            return ResponseResult.fail("请先选择商品，并指定要执行的上架/下架操作");
        }
        // 去重 + 过滤空值，避免前端传重复 id 导致"影响条数"对不上
        List<Integer> ids = goods.getIds().stream()
                .filter(Objects::nonNull)
                .distinct()
                .collect(Collectors.toList());
        if (ids.isEmpty()) {
            return ResponseResult.fail("请先选择商品");
        }
        int isoff = (goods.getIsoff() == 0) ? 0 : 1;

        // 上架前校验库存：库存为 0 的商品不允许上架
        // （下单扣库存时库存清零会自动下架，这里保持同一口径，避免出现"上架了但买不了"）
        String tip = "";
        if (isoff == 0) {
            List<Integer> noStockIds = listByIds(ids).stream()
                    .filter(g -> g.getStock() == null || g.getStock() <= 0)
                    .map(Goods::getId)
                    .collect(Collectors.toList());
            if (!noStockIds.isEmpty()) {
                ids.removeAll(noStockIds);
            }
            if (ids.isEmpty()) {
                return ResponseResult.fail("所选商品库存均为 0，无法上架，请先补充库存");
            }
            if (!noStockIds.isEmpty()) {
                tip = "（另有 " + noStockIds.size() + " 件商品因库存为 0 被跳过）";
            }
        }

        UpdateWrapper<Goods> updateWrapper = new UpdateWrapper<>();
        updateWrapper.in("id", ids).set("isoff", isoff);
        if (update(updateWrapper)) {
            return ResponseResult.success(
                    (isoff == 0 ? "已批量上架 " : "已批量下架 ") + ids.size() + " 件商品" + tip, null);
        }
        return ResponseResult.fail("批量操作失败");
    }

    // ==================== 前台浏览 ====================

    @Override
    public ResponseResult<Object> getGoodsIndex(Integer currentPage, Goods goods, String sortBy) {
        int page = (currentPage == null || currentPage < 1) ? 1 : currentPage;
        Page<Goods> pageParam = new Page<>(page, FRONT_PAGE_SIZE);

        QueryWrapper<Goods> wrapper = new QueryWrapper<>();
        // 前台只看上架商品
        wrapper.eq("isoff", 0);
        if (goods != null) {
            if (goods.getGoodstypeId() != null && goods.getGoodstypeId() > 0) {
                wrapper.eq("goodstype_id", goods.getGoodstypeId());
            }
            if (StrUtil.isNotBlank(goods.getKeywords())) {
                wrapper.like("name", goods.getKeywords().trim());
            }
        }
        // 排序
        if ("priceAsc".equalsIgnoreCase(sortBy)) {
            wrapper.orderByAsc("price");
        } else if ("priceDesc".equalsIgnoreCase(sortBy)) {
            wrapper.orderByDesc("price");
        } else if ("salesDesc".equalsIgnoreCase(sortBy)) {
            wrapper.orderByDesc("sales");
        } else {
            wrapper.orderByDesc("id");
        }

        Page<Goods> result = page(pageParam, wrapper);
        fillTypeName(result.getRecords());

        Map<String, Object> data = new HashMap<>(4);
        data.put("goodsList", result.getRecords());
        data.put("totalPage", result.getPages());
        data.put("total", result.getTotal());
        data.put("currentPage", page);
        return ResponseResult.success(data);
    }

    @Override
    public ResponseResult<Goods> getGoodsDetail(Integer id) {
        if (id == null) {
            return ResponseResult.fail("商品 ID 不能为空");
        }
        Goods goods = getById(id);
        if (goods == null) {
            return ResponseResult.fail("商品不存在或已下架");
        }
        // 填充类型名称，前端详情页展示更友好
        GoodsType type = goodsTypeMapper.selectById(goods.getGoodstypeId());
        if (type != null) {
            goods.setTypeName(type.getName());
        }
        return ResponseResult.success(goods);
    }

    @Override
    public ResponseResult<List<Goods>> getRecommendGoods(Integer limit) {
        int size = (limit == null || limit < 1) ? 8 : limit;
        QueryWrapper<Goods> wrapper = new QueryWrapper<>();
        wrapper.eq("isoff", 0)
                .orderByDesc("sales")
                .last("LIMIT " + size);
        return ResponseResult.success(list(wrapper));
    }

    // ==================== 统计 ====================

    @Override
    public ResponseResult<List<Map<String, Object>>> getSalesByType() {
        return ResponseResult.success(baseMapper.getSalesByType());
    }

    @Override
    public ResponseResult<List<Map<String, Object>>> getHotGoods(Integer limit) {
        int size = (limit == null || limit < 1) ? 10 : limit;
        return ResponseResult.success(baseMapper.getHotGoods(size));
    }

    @Override
    public ResponseResult<List<Map<String, Object>>> getSalesByMonth(Integer months) {
        // 限制在 1~24 个月，避免前端传个离谱的值把图表撑爆
        int size = (months == null || months < 1) ? 6 : Math.min(months, 24);
        YearMonth start = YearMonth.now().minusMonths(size - 1L);

        List<Map<String, Object>> raw = baseMapper.getSalesByMonth(start.atDay(1).toString());

        // SQL 只返回有成交的月份，这里按 yyyy-MM 建索引方便回填
        Map<String, Map<String, Object>> byMonth = new HashMap<>(raw.size());
        for (Map<String, Object> row : raw) {
            byMonth.put(String.valueOf(row.get("month")), row);
        }

        // 补齐没有成交的月份，让折线的 X 轴是连续的，
        // 否则"某个月零成交"会从图上消失，趋势被看错
        List<Map<String, Object>> filled = new ArrayList<>(size);
        for (int i = 0; i < size; i++) {
            String key = start.plusMonths(i).toString(); // YearMonth#toString → yyyy-MM
            Map<String, Object> row = byMonth.get(key);
            if (row == null) {
                row = new HashMap<>(4);
                row.put("month", key);
                row.put("totalAmount", BigDecimal.ZERO);
                row.put("totalNum", 0);
            }
            filled.add(row);
        }
        return ResponseResult.success(filled);
    }

    @Override
    public ResponseResult<Map<String, Object>> getDashboard() {
        Map<String, Object> data = new HashMap<>(8);
        // 商品总数（只统计上架的）
        data.put("goodsCount", count(new QueryWrapper<Goods>().eq("isoff", 0)));
        // 用户总数
        data.put("userCount", bUserMapper.selectCount(null));
        // 订单总数
        data.put("orderCount", orderBaseMapper.selectCount(null));
        // 销售总额（排除待付款与已取消）
        QueryWrapper<OrderBase> amountWrapper = new QueryWrapper<>();
        amountWrapper.in("status", 2, 3, 4).select("IFNULL(SUM(amount), 0) AS total");
        List<Object> amounts = orderBaseMapper.selectObjs(amountWrapper);
        data.put("totalAmount", amounts.isEmpty() || amounts.get(0) == null ? 0 : amounts.get(0));
        // 待处理订单数（待付款 + 已付款待发货）
        data.put("pendingOrderCount", orderBaseMapper.selectCount(
                new QueryWrapper<OrderBase>().in("status", 1, 2)));
        return ResponseResult.success(data);
    }

    // ==================== 私有辅助方法 ====================

    /**
     * 为商品列表填充类型名称
     */
    private void fillTypeName(List<Goods> goodsList) {
        if (goodsList == null || goodsList.isEmpty()) {
            return;
        }
        // 先查出全部类型做成 Map，避免循环里反复查库
        List<GoodsType> types = goodsTypeMapper.selectList(null);
        Map<Integer, String> typeMap = new HashMap<>(types.size());
        for (GoodsType t : types) {
            typeMap.put(t.getId(), t.getName());
        }
        for (Goods g : goodsList) {
            g.setTypeName(typeMap.get(g.getGoodstypeId()));
        }
    }
}
