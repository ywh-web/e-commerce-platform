package com.ebusiness.common.util;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.Date;

/**
 * 文件上传工具类
 *
 * <p>商品图片上传的处理流程：
 * <ol>
 *   <li>校验文件非空、扩展名合法</li>
 *   <li>用 UUID 重命名，避免同名覆盖</li>
 *   <li>写入后端 upload 目录</li>
 *   <li>返回新文件名，供前端拼接访问地址</li>
 * </ol>
 *
 * @author 杨伟宏
 */
public class UploadUtil {

    /** 允许的图片扩展名 */
    private static final String[] ALLOW_EXT = {"jpg", "jpeg", "png", "gif", "bmp", "webp"};

    /**
     * 保存上传的图片
     *
     * @param file   上传的文件
     * @param saveDir 保存目录的绝对路径（末尾带 /）
     * @return 重命名后的文件名；文件为空时返回 null
     * @throws IOException 写入失败时抛出
     */
    public static String savePicture(MultipartFile file, String saveDir) throws IOException {
        if (file == null || file.isEmpty()) {
            return null;
        }
        String originalName = file.getOriginalFilename();
        if (StrUtil.isBlank(originalName)) {
            return null;
        }
        // 扩展名合法性校验
        String ext = FileUtil.extName(originalName);
        if (!isAllowExt(ext)) {
            throw new IOException("不支持的图片格式：" + ext);
        }
        // UUID 重命名，形如 20251009153012_a1b2c3d4.jpg
        String newName = DateUtil.format(new Date(), "yyyyMMddHHmmss")
                + "_" + IdUtil.fastSimpleUUID().substring(0, 8)
                + "." + ext.toLowerCase();

        // 目录不存在则创建
        File dir = new File(saveDir);
        if (!dir.exists() && !dir.mkdirs()) {
            throw new IOException("创建上传目录失败：" + saveDir);
        }
        file.transferTo(new File(dir, newName));
        return newName;
    }

    /**
     * 判断扩展名是否在白名单内
     */
    private static boolean isAllowExt(String ext) {
        if (StrUtil.isBlank(ext)) {
            return false;
        }
        for (String allow : ALLOW_EXT) {
            if (allow.equalsIgnoreCase(ext)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 生成基于时间戳的业务订单号
     *
     * @return 形如 20251009153012 的 14 位订单号
     */
    public static String generateOrderNumber() {
        return DateUtil.format(new Date(), "yyyyMMddHHmmss")
                + (int) ((Math.random() * 9 + 1) * 1000);
    }
}
