package com.ebusiness.controller.admin;

import com.ebusiness.common.util.ResponseResult;
import com.ebusiness.entity.Goods;
import com.ebusiness.service.GoodsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

/**
 * 后台商品管理控制器
 *
 * <p>接口前缀：/api/admin/goods
 *
 * @author 杨伟宏
 */
@RestController("adminGoodsController")
@RequestMapping("/api/admin/goods")
public class GoodsController {

    @Autowired
    private GoodsService goodsService;

    /**
     * 商品列表（分页 + 类型筛选 + 关键词搜索）
     *
     * <p>GET /api/admin/goods/list?currentPage=1&goodstypeId=1&keywords=华为
     */
    @GetMapping("/list")
    public ResponseResult<Object> list(@RequestParam(defaultValue = "1") Integer currentPage,
                                       @RequestParam(required = false) Integer goodstypeId,
                                       @RequestParam(required = false) String keywords) {
        Goods query = new Goods();
        query.setGoodstypeId(goodstypeId);
        query.setKeywords(keywords);
        return goodsService.getAllGoods(currentPage, query);
    }

    /**
     * 新增商品（表单方式，含图片上传）
     *
     * <p>POST /api/admin/goods/add
     */
    @PostMapping("/add")
    public ResponseResult<Object> add(Goods goods,
                                      @RequestParam(value = "file", required = false) MultipartFile file) {
        goods.setAct("add");
        if (file != null && !file.isEmpty()) {
            goods.setLogoFile(file);
        }
        return goodsService.addOrUpdate(goods);
    }

    /**
     * 修改商品（表单方式，含图片上传）
     *
     * <p>POST /api/admin/goods/update
     */
    @PostMapping("/update")
    public ResponseResult<Object> update(Goods goods,
                                         @RequestParam(value = "file", required = false) MultipartFile file) {
        goods.setAct("update");
        if (file != null && !file.isEmpty()) {
            goods.setLogoFile(file);
        }
        return goodsService.addOrUpdate(goods);
    }

    /**
     * 删除商品
     *
     * <p>DELETE /api/admin/goods/delete/{id}
     */
    @DeleteMapping("/delete/{id}")
    public ResponseResult<Object> delete(@PathVariable Integer id) {
        Goods goods = new Goods();
        goods.setId(id);
        return goodsService.delete(goods);
    }

    /**
     * 上架 / 下架
     *
     * <p>POST /api/admin/goods/status
     * <p>请求体：{ "id": 1, "isoff": 1 }
     */
    @PostMapping("/status")
    public ResponseResult<Object> changeStatus(@RequestBody Goods goods) {
        return goodsService.changeStatus(goods);
    }

    /**
     * 批量上架 / 下架
     *
     * <p>POST /api/admin/goods/batchStatus
     * <p>请求体：{ "ids": [1,2,3], "isoff": 1 }
     * <p>注意：批量上架时会自动跳过库存为 0 的商品
     */
    @PostMapping("/batchStatus")
    public ResponseResult<Object> batchStatus(@RequestBody Goods goods) {
        return goodsService.batchChangeStatus(goods);
    }

    /**
     * 商品详情（后台编辑回显用）
     *
     * <p>GET /api/admin/goods/detail/{id}
     */
    @GetMapping("/detail/{id}")
    public ResponseResult<Object> detail(@PathVariable Integer id) {
        return ResponseResult.success(goodsService.getGoodsDetail(id).getData());
    }
}
