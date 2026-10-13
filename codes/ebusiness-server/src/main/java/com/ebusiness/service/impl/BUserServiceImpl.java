package com.ebusiness.service.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ebusiness.common.security.JwtTokenUtil;
import com.ebusiness.common.util.MD5Util;
import com.ebusiness.common.util.ResponseResult;
import com.ebusiness.entity.BUser;
import com.ebusiness.entity.OrderBase;
import com.ebusiness.mapper.BUserMapper;
import com.ebusiness.mapper.OrderBaseMapper;
import com.ebusiness.service.BUserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 前台用户业务实现
 *
 * @author 杨伟宏
 */
@Slf4j
@Service
public class BUserServiceImpl extends ServiceImpl<BUserMapper, BUser> implements BUserService {

    /** 每页条数 */
    private static final int PAGE_SIZE = 10;

    @Autowired
    private JwtTokenUtil jwtTokenUtil;

    @Autowired
    private com.ebusiness.common.util.CaptchaCodeUtil captchaUtil;

    @Autowired
    private OrderBaseMapper orderBaseMapper;

    @Override
    public ResponseResult<Object> register(BUser bUser) {
        if (bUser == null || StrUtil.isBlank(bUser.getBemail()) || StrUtil.isBlank(bUser.getBpwd())) {
            return ResponseResult.fail("邮箱和密码不能为空");
        }

        String bemail = bUser.getBemail().trim();
        // 1. 邮箱格式基础校验
        if (!bemail.matches("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")) {
            return ResponseResult.fail("邮箱格式不正确");
        }
        // 2. 密码长度校验
        if (bUser.getBpwd().length() < 6) {
            return ResponseResult.fail("密码长度不能少于 6 位");
        }
        // 3. 邮箱是否已注册
        QueryWrapper<BUser> wrapper = new QueryWrapper<>();
        wrapper.eq("bemail", bemail);
        if (count(wrapper) > 0) {
            return ResponseResult.fail("该邮箱已注册，请直接登录");
        }

        // 4. 密码加密后入库
        bUser.setBemail(bemail);
        bUser.setBpwd(MD5Util.md5(bUser.getBpwd()));
        if (StrUtil.isBlank(bUser.getBname())) {
            // 未填昵称时用邮箱 @ 前的部分作默认昵称
            bUser.setBname(bemail.substring(0, bemail.indexOf('@')));
        }
        if (!save(bUser)) {
            return ResponseResult.fail("注册失败，请稍后重试");
        }
        log.info("新用户注册成功：{}", bemail);
        return ResponseResult.success("注册成功，请登录", null);
    }

    @Override
    public ResponseResult<Object> login(BUser bUser) {
        if (bUser == null || StrUtil.isBlank(bUser.getBemail()) || StrUtil.isBlank(bUser.getBpwd())) {
            return ResponseResult.fail("邮箱和密码不能为空");
        }

        String bemail = bUser.getBemail().trim();

        // 1. 查用户是否存在
        QueryWrapper<BUser> wrapper = new QueryWrapper<>();
        wrapper.eq("bemail", bemail);
        BUser dbUser = getOne(wrapper);
        if (dbUser == null) {
            return ResponseResult.fail("该邮箱尚未注册");
        }

        // 2. 校验密码
        String inputMd5 = MD5Util.md5(bUser.getBpwd());
        if (!inputMd5.equalsIgnoreCase(dbUser.getBpwd())) {
            return ResponseResult.fail("密码错误，请重新输入");
        }

        // 3. 签发 token
        String token = jwtTokenUtil.createToken(dbUser.getBemail(), JwtTokenUtil.ROLE_USER, dbUser.getBid());

        Map<String, Object> data = new HashMap<>(8);
        data.put("token", token);
        data.put("bemail", dbUser.getBemail());
        data.put("bname", dbUser.getBname());
        data.put("bid", dbUser.getBid());
        data.put("role", JwtTokenUtil.ROLE_USER);

        log.info("用户登录成功：{}", bemail);
        return ResponseResult.success("登录成功", data);
    }

    @Override
    public ResponseResult<Object> getInfo(Integer bid) {
        if (bid == null) {
            return ResponseResult.fail("用户 ID 不能为空");
        }
        BUser user = getById(bid);
        if (user == null) {
            return ResponseResult.fail("用户不存在");
        }
        // 清空密码，绝不返回给前端
        user.setBpwd(null);
        return ResponseResult.success(user);
    }

    @Override
    public ResponseResult<Object> updateInfo(BUser bUser) {
        if (bUser == null || bUser.getBid() == null) {
            return ResponseResult.fail("用户 ID 不能为空");
        }
        BUser dbUser = getById(bUser.getBid());
        if (dbUser == null) {
            return ResponseResult.fail("用户不存在");
        }
        // 只允许修改这几项，邮箱与密码有独立接口，此处不透传
        BUser update = new BUser();
        update.setBid(bUser.getBid());
        update.setBname(bUser.getBname());
        update.setBphone(bUser.getBphone());
        update.setBaddr(bUser.getBaddr());
        if (updateById(update)) {
            return ResponseResult.success("资料修改成功", null);
        }
        return ResponseResult.fail("资料修改失败");
    }

    @Override
    public ResponseResult<Object> updatePassword(Integer bid, String oldPwd, String newPwd) {
        if (bid == null || StrUtil.isBlank(oldPwd) || StrUtil.isBlank(newPwd)) {
            return ResponseResult.fail("原密码与新密码不能为空");
        }
        if (newPwd.length() < 6) {
            return ResponseResult.fail("新密码长度不能少于 6 位");
        }
        BUser dbUser = getById(bid);
        if (dbUser == null) {
            return ResponseResult.fail("用户不存在");
        }
        // 校验原密码
        if (!MD5Util.md5(oldPwd).equalsIgnoreCase(dbUser.getBpwd())) {
            return ResponseResult.fail("原密码不正确");
        }
        BUser update = new BUser();
        update.setBid(bid);
        update.setBpwd(MD5Util.md5(newPwd));
        if (updateById(update)) {
            return ResponseResult.success("密码修改成功，请重新登录", null);
        }
        return ResponseResult.fail("密码修改失败");
    }

    @Override
    public ResponseResult<Object> getAllUsers(Integer currentPage, BUser bUser) {
        int page = (currentPage == null || currentPage < 1) ? 1 : currentPage;
        Page<BUser> pageParam = new Page<>(page, PAGE_SIZE);

        QueryWrapper<BUser> wrapper = new QueryWrapper<>();
        if (bUser != null) {
            if (StrUtil.isNotBlank(bUser.getBemail())) {
                wrapper.like("bemail", bUser.getBemail().trim());
            }
            if (StrUtil.isNotBlank(bUser.getBname())) {
                wrapper.like("bname", bUser.getBname().trim());
            }
        }
        wrapper.orderByDesc("bid");

        Page<BUser> result = page(pageParam, wrapper);
        // 列表里的密码全部置空
        List<BUser> records = result.getRecords();
        records.forEach(u -> u.setBpwd(null));

        Map<String, Object> data = new HashMap<>(4);
        data.put("users", records);
        data.put("totalPage", result.getPages());
        data.put("total", result.getTotal());
        data.put("currentPage", page);
        return ResponseResult.success(data);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ResponseResult<Object> delete(BUser bUser) {
        if (bUser == null || bUser.getBid() == null) {
            return ResponseResult.fail("用户 ID 不能为空");
        }
        Integer bid = bUser.getBid();

        // 该用户有订单时不允许删除，避免破坏外键约束与历史数据
        Long orderCount = orderBaseMapper.selectCount(
                new QueryWrapper<OrderBase>().eq("busertable_id", bid));
        if (orderCount != null && orderCount > 0) {
            return ResponseResult.fail("该用户存在 " + orderCount + " 条订单，不允许删除");
        }

        if (removeById(bid)) {
            return ResponseResult.success("用户删除成功", null);
        }
        return ResponseResult.fail("用户删除失败");
    }
}
