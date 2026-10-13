package com.ebusiness.common.security;

/**
 * 当前登录用户上下文（基于 ThreadLocal）
 *
 * <p>拦截器校验 token 通过后，把账号/角色/主键写入本类；
 * Controller 与 Service 无需再解析 token，直接读取即可。
 * <p>请求结束后由拦截器调用 {@link #clear()} 释放，避免线程池复用导致数据串号。
 *
 * @author 杨伟宏
 */
public class UserContext {

    /** 使用 ThreadLocal 保证每个请求线程独立 */
    private static final ThreadLocal<LoginUser> HOLDER = new ThreadLocal<>();

    /**
     * 存入当前登录用户
     */
    public static void set(LoginUser user) {
        HOLDER.set(user);
    }

    /**
     * 获取当前登录用户
     *
     * @return 未登录时返回 null
     */
    public static LoginUser get() {
        return HOLDER.get();
    }

    /**
     * 获取当前登录用户的主键 ID
     *
     * @return 未登录时返回 null
     */
    public static Integer getUid() {
        LoginUser user = HOLDER.get();
        return user == null ? null : user.getUid();
    }

    /**
     * 获取当前登录账号
     */
    public static String getAccount() {
        LoginUser user = HOLDER.get();
        return user == null ? null : user.getAccount();
    }

    /**
     * 获取当前登录角色：admin / user
     */
    public static String getRole() {
        LoginUser user = HOLDER.get();
        return user == null ? null : user.getRole();
    }

    /**
     * 判断当前是否管理员
     */
    public static boolean isAdmin() {
        return JwtTokenUtil.ROLE_ADMIN.equals(getRole());
    }

    /**
     * 清理上下文（请求结束时必须调用）
     */
    public static void clear() {
        HOLDER.remove();
    }

    /**
     * 登录用户信息载体
     */
    public static class LoginUser {
        private String account;
        private String role;
        private Integer uid;

        public LoginUser() {
        }

        public LoginUser(String account, String role, Integer uid) {
            this.account = account;
            this.role = role;
            this.uid = uid;
        }

        public String getAccount() {
            return account;
        }

        public void setAccount(String account) {
            this.account = account;
        }

        public String getRole() {
            return role;
        }

        public void setRole(String role) {
            this.role = role;
        }

        public Integer getUid() {
            return uid;
        }

        public void setUid(Integer uid) {
            this.uid = uid;
        }
    }
}
