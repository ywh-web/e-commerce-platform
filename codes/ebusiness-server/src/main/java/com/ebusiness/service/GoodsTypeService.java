package com.ebusiness.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.ebusiness.common.util.ResponseResult;
import com.ebusiness.entity.GoodsType;

import java.util.List;

/**
 * 商品类型业务接口
 *
 * @author 杨伟宏
 */
public interface GoodsTypeService extends IService<GoodsType> {

    /**
     * 查询全部商品类型（前台筛选栏 / 后台下拉框用）
     *
     * @return 类型列表，每项附带该类型下的商品数量
     */
    ResponseResult<List<GoodsType>> getAllTypes();

    /**
     * 分页查询商品类型（后台类型管理列表用）
     *
     * @param currentPage 页码，从 1 开始
     * @param keywords    类型名称关键词，可为空
     * @return data 含 typeList 数组、total 总条数、totalPage 总页数、currentPage 当前页
     */
    ResponseResult<Object> getTypesByPage(Integer currentPage, String keywords);

    /**
     * 新增类型
     *
     * @param goodsType 含 name
     * @return 操作结果；同名类型不允许重复添加
     */
    ResponseResult<Object> add(GoodsType goodsType);

    /**
     * 修改类型名称
     *
     * @param goodsType 含 id 与 name
     * @return 操作结果
     */
    ResponseResult<Object> update(GoodsType goodsType);

    /**
     * 删除类型
     *
     * @param goodsType 含 id
     * @return 操作结果；该类型下有商品时不允许删除
     */
    ResponseResult<Object> delete(GoodsType goodsType);
}
