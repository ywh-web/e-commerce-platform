package com.ebusiness.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.ebusiness.common.util.ResponseResult;
import com.ebusiness.entity.Focus;

/**
 * 商品收藏业务接口
 *
 * @author 杨伟宏
 */
public interface FocusService extends IService<Focus> {

    /**
     * 收藏 / 取消收藏（切换式）
     *
     * <p>已收藏则取消，未收藏则新增。前端只需调这一个接口，
     * 返回 data 中的 focused 字段告诉前端当前状态。
     *
     * @param focus 含 busertableId、goodstableId
     * @return data 中含 focused（true 已收藏 / false 已取消）
     */
    ResponseResult<Object> toggleFocus(Focus focus);

    /**
     * 查询某用户的收藏列表（联表带出商品信息）
     *
     * @param busertableId 用户 ID
     * @return 收藏列表
     */
    ResponseResult<Object> getFocusByUid(Integer busertableId);

    /**
     * 判断某用户是否已收藏某商品
     *
     * @param busertableId 用户 ID
     * @param goodstableId 商品 ID
     * @return data 中为 true / false
     */
    ResponseResult<Object> isFocused(Integer busertableId, Integer goodstableId);

    /**
     * 删除收藏记录
     *
     * @param fid 收藏记录 ID
     * @return 操作结果
     */
    ResponseResult<Object> deleteFocus(Integer fid);
}
