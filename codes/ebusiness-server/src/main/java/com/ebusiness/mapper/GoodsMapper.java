package com.ebusiness.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.ebusiness.entity.Goods;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

/**
 * 商品 Mapper
 *
 * <p>除 BaseMapper 提供的单表 CRUD 外，这里补充统计类自定义 SQL。
 *
 * @author 杨伟宏
 */
public interface GoodsMapper extends BaseMapper<Goods> {

    /**
     * 按类型统计商品销售情况 —— 供后台"销售统计"图表使用
     *
     * <p>统计口径：遍历已付款及之后状态的订单，按商品所属类型汇总销售金额与销量。
     *
     * @return 每行包含 typeName（类型名称）、totalAmount（销售总额）、totalNum（销售件数）
     */
    List<Map<String, Object>> getSalesByType();

    /**
     * 查询热销商品 TOP N —— 供后台"订单统计"与前台推荐位使用
     *
     * @param limit 返回条数
     * @return 每行包含 goodsName、price、sales、totalAmount
     */
    List<Map<String, Object>> getHotGoods(@Param("limit") Integer limit);

    /**
     * 按自然月统计销售情况 —— 供后台"销售统计"月度图表使用
     *
     * <p>统计口径与其它报表一致：只累加 status IN (2,3,4) 的有效订单。
     * <p>只返回有成交的月份，没有成交的月份由 Service 层补 0，保证 X 轴连续。
     *
     * @param startMonth 起始月份（含），格式 yyyy-MM-01
     * @return 每行包含 month（yyyy-MM）、totalAmount（成交金额）、totalNum（成交件数）
     */
    List<Map<String, Object>> getSalesByMonth(@Param("startMonth") String startMonth);
}
