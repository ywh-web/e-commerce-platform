package com.ebusiness.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.ebusiness.common.util.ResponseResult;
import com.ebusiness.entity.Goods;

import java.util.List;
import java.util.Map;

/**
 * 商品业务接口
 *
 * @author 杨伟宏
 */
public interface GoodsService extends IService<Goods> {

    // ==================== 后台管理 ====================

    /**
     * 后台商品列表（分页 + 类型筛选 + 关键词搜索）
     *
     * @param currentPage 页码，从 1 开始
     * @param goods       筛选条件：goodstypeId（类型）、keywords（名称关键词）
     * @return data 含 goodsList 数组与 totalPage 总页数
     */
    ResponseResult<Object> getAllGoods(Integer currentPage, Goods goods);

    /**
     * 新增 / 修改商品（由 goods.act 区分："add" 表示新增）
     *
     * @param goods 商品信息，含上传的图片文件 logoFile
     * @return 操作结果
     */
    ResponseResult<Object> addOrUpdate(Goods goods);

    /**
     * 删除商品
     *
     * @param goods 含 id
     * @return 操作结果；该商品已被加购/收藏/下单时不允许删除
     */
    ResponseResult<Object> delete(Goods goods);

    /**
     * 商品上架 / 下架
     *
     * @param goods 含 id 与 isoff
     * @return 操作结果
     */
    ResponseResult<Object> changeStatus(Goods goods);

    /**
     * 商品批量上架 / 下架
     *
     * @param goods 含 ids（商品 ID 数组）与 isoff（0-上架 1-下架）
     * @return 操作结果，消息里带上成功影响的条数
     */
    ResponseResult<Object> batchChangeStatus(Goods goods);

    // ==================== 前台浏览 ====================

    /**
     * 前台商品列表（分页 + 类型筛选 + 关键词搜索 + 排序）
     *
     * <p>只返回上架商品；游客无需登录即可调用。
     *
     * @param currentPage 页码
     * @param goods       筛选条件：goodstypeId、keywords
     * @param sortBy      排序方式：priceAsc / priceDesc / salesDesc，为空则按 id 倒序
     * @return data 含 goodsList 与 totalPage
     */
    ResponseResult<Object> getGoodsIndex(Integer currentPage, Goods goods, String sortBy);

    /**
     * 前台商品详情
     *
     * @param id 商品 ID
     * @return 单个商品信息
     */
    ResponseResult<Goods> getGoodsDetail(Integer id);

    /**
     * 首页推荐商品（按销量倒序取前 N 条）
     *
     * @param limit 条数
     * @return 商品列表
     */
    ResponseResult<List<Goods>> getRecommendGoods(Integer limit);

    // ==================== 统计 ====================

    /**
     * 按类型统计销售情况（后台销售统计柱状图）
     */
    ResponseResult<List<Map<String, Object>>> getSalesByType();

    /**
     * 热销商品 TOP N
     */
    ResponseResult<List<Map<String, Object>>> getHotGoods(Integer limit);

    /**
     * 按自然月统计销售情况（后台销售统计月度图表）
     *
     * @param months 统计最近几个月，默认 6
     * @return 每个月一条：month（yyyy-MM）、totalAmount、totalNum；
     *         没有成交的月份也会补 0 返回，保证图表 X 轴连续
     */
    ResponseResult<List<Map<String, Object>>> getSalesByMonth(Integer months);

    /**
     * 后台首页概览数据：商品总数、用户总数、订单总数、销售总额
     */
    ResponseResult<Map<String, Object>> getDashboard();
}
