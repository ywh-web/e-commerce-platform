package com.ebusiness.controller.admin;

import com.ebusiness.common.util.ResponseResult;
import com.ebusiness.entity.GoodsType;
import com.ebusiness.service.GoodsTypeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 后台商品类型管理控制器
 *
 * <p>接口前缀：/api/admin/type
 *
 * @author 杨伟宏
 */
@RestController
@RequestMapping("/api/admin/type")
public class GoodsTypeController {

    @Autowired
    private GoodsTypeService goodsTypeService;

    /**
     * 查询全部商品类型（下拉框、前台筛选栏用，不分页）
     *
     * <p>GET /api/admin/type/list
     */
    @GetMapping("/list")
    public ResponseResult<Object> list() {
        return ResponseResult.success(goodsTypeService.getAllTypes().getData());
    }

    /**
     * 分页查询商品类型（后台类型管理列表用）
     *
     * <p>GET /api/admin/type/page?currentPage=1&keywords=手机
     * <p>返回 data：{ typeList, total, totalPage, currentPage }
     */
    @GetMapping("/page")
    public ResponseResult<Object> page(@RequestParam(defaultValue = "1") Integer currentPage,
                                       @RequestParam(required = false) String keywords) {
        return goodsTypeService.getTypesByPage(currentPage, keywords);
    }

    /**
     * 新增类型
     *
     * <p>POST /api/admin/type/add
     * <p>请求体：{ "name": "智能家居" }
     */
    @PostMapping("/add")
    public ResponseResult<Object> add(@RequestBody GoodsType goodsType) {
        return goodsTypeService.add(goodsType);
    }

    /**
     * 修改类型名称
     *
     * <p>POST /api/admin/type/update
     * <p>请求体：{ "id": 1, "name": "手机数码" }
     */
    @PostMapping("/update")
    public ResponseResult<Object> update(@RequestBody GoodsType goodsType) {
        return goodsTypeService.update(goodsType);
    }

    /**
     * 删除类型
     *
     * <p>DELETE /api/admin/type/delete/{id}
     */
    @DeleteMapping("/delete/{id}")
    public ResponseResult<Object> delete(@PathVariable Integer id) {
        GoodsType goodsType = new GoodsType();
        goodsType.setId(id);
        return goodsTypeService.delete(goodsType);
    }
}
