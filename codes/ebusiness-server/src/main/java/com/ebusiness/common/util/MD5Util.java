package com.ebusiness.common.util;

import cn.hutool.crypto.digest.DigestUtil;
import cn.hutool.core.util.StrUtil;

/**
 * MD5 加密工具类
 *
 * <p>用户与管理员密码在入库前统一用本类加密，数据库中不存明文。
 * <p>返回小写 32 位十六进制字符串，与 SQL 脚本中预置的
 * {@code e10adc3949ba59abbe56e057f20f883e}（即 "123456"）保持一致。
 *
 * @author 杨伟宏
 */
public class MD5Util {

    /** 固定盐值：加盐后即使两个用户密码相同，密文也不同，安全性略高于裸 MD5 */
    private static final String SALT = "ebusiness@2025";

    /**
     * 对明文密码加密（带固定盐）
     *
     * @param plainText 明文密码
     * @return 小写 32 位 MD5 密文；入参为空时原样返回
     */
    public static String encrypt(String plainText) {
        if (StrUtil.isBlank(plainText)) {
            return plainText;
        }
        return DigestUtil.md5Hex(plainText + SALT);
    }

    /**
     * 对明文做标准 MD5（不加盐）
     *
     * <p>用于与 SQL 脚本中预置的密文比对场景（如 123456 → e10adc39...）。
     *
     * @param plainText 明文
     * @return 小写 32 位 MD5 密文
     */
    public static String md5(String plainText) {
        if (StrUtil.isBlank(plainText)) {
            return plainText;
        }
        return DigestUtil.md5Hex(plainText);
    }

    /**
     * 校验明文与密文是否匹配（不加盐口径）
     *
     * @param plainText  明文
     * @param cipherText 数据库中保存的密文
     * @return true 表示匹配
     */
    public static boolean verify(String plainText, String cipherText) {
        if (StrUtil.isBlank(plainText) || StrUtil.isBlank(cipherText)) {
            return false;
        }
        return md5(plainText).equalsIgnoreCase(cipherText);
    }
}
