package com.ebusiness.service.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ebusiness.common.security.JwtTokenUtil;
import com.ebusiness.common.util.CaptchaCodeUtil;
import com.ebusiness.common.util.MD5Util;
import com.ebusiness.common.util.ResponseResult;
import com.ebusiness.entity.AUser;
import com.ebusiness.mapper.AUserMapper;
import com.ebusiness.service.AUserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

/**
 * 管理员业务实现
 *
 * @author 杨伟宏
 */
@Slf4j
@Service
public class AUserServiceImpl extends ServiceImpl<AUserMapper, AUser> implements AUserService {

    @Autowired
    private JwtTokenUtil jwtTokenUtil;

    @Autowired
    private CaptchaCodeUtil captchaUtil;

    @Override
    public ResponseResult<Object> login(AUser aUser) {
        if (aUser == null || StrUtil.isBlank(aUser.getAname()) || StrUtil.isBlank(aUser.getApwd())) {
            return ResponseResult.fail("账号和密码不能为空");
        }

        // 1. 按账号查询管理员
        QueryWrapper<AUser> wrapper = new QueryWrapper<>();
        wrapper.eq("aname", aUser.getAname().trim());
        AUser dbUser = getOne(wrapper);
        if (dbUser == null) {
            return ResponseResult.fail("管理员账号不存在");
        }

        // 2. 校验密码（数据库存 MD5，采用不加盐口径以兼容 SQL 预置数据）
        String inputMd5 = MD5Util.md5(aUser.getApwd());
        if (!inputMd5.equalsIgnoreCase(dbUser.getApwd())) {
            return ResponseResult.fail("密码错误，请重新输入");
        }

        // 3. 密码正确，签发 token
        String token = jwtTokenUtil.createToken(dbUser.getAname(), JwtTokenUtil.ROLE_ADMIN, dbUser.getAid());

        Map<String, Object> data = new HashMap<>(8);
        data.put("token", token);
        data.put("aname", dbUser.getAname());
        data.put("aid", dbUser.getAid());
        data.put("role", JwtTokenUtil.ROLE_ADMIN);

        log.info("管理员登录成功：{}", dbUser.getAname());
        return ResponseResult.success("登录成功", data);
    }
}
