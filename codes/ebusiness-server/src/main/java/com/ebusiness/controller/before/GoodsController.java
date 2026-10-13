package com.ebusiness.controller.before;

import com.ebusiness.common.util.ResponseResult;
import com.ebusiness.entity.Goods;
import com.ebusiness.service.GoodsService;
import com.ebusiness.service.GoodsTypeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 前台商品浏览控制器
 *
 * <p>接口前缀：/api/before/goods
 * <p>注意：本控制器的接口在拦截器白名单内，游客（未登录）也能访问。
 *
 * @author 杨伟宏
 */
@RestController("beforeGoodsController")
@RequestMapping("/api/before/goods")
public class GoodsController {

    @Autowired
    private GoodsService goodsService;

    @Autowired
    private GoodsTypeService goodsTypeService;

    /**
     * 商品列表（分页 + 类型筛选 + 关键词搜索 + 排序）
     *
     * <p>GET /api/before/goods/list?currentPage=1&goodstypeId=1&keywords=华为&sortBy=salesDesc
     */
    @GetMapping("/list")
    public ResponseResult<Object> list(@RequestParam(defaultValue = "1") Integer currentPage,
                                       @RequestParam(required = false) Integer goodstypeId,
                                       @RequestParam(required = false) String keywords,
                                       @RequestParam(required = false) String sortBy) {
        Goods query = new Goods();
        query.setGoodstypeId(goodstypeId);
        query.setKeywords(keywords);
        return goodsService.getGoodsIndex(currentPage, query, sortBy);
    }

    /**
     * 商品详情
     *
     * <p>GET /api/before/goods/detail/{id}
     */
    @GetMapping("/detail/{id}")
    public ResponseResult<Goods> detail(@PathVariable Integer id) {
        return goodsService.getGoodsDetail(id);
    }

    /**
     * 首页推荐商品（按销量倒序）
     *
     * <p>GET /api/before/goods/recommend?limit=8
     */
    @GetMapping("/recommend")
    public ResponseResult<Object> recommend(@RequestParam(defaultValue = "8") Integer limit) {
        return ResponseResult.success(goodsService.getRecommendGoods(limit).getData());
    }

    /**
     * 商品类型列表（前台筛选栏用）
     *
     * <p>GET /api/before/goods/types
     */
    @GetMapping("/types")
    public ResponseResult<Object> types() {
        return ResponseResult.success(goodsTypeService.getAllTypes().getData());
    }
}
