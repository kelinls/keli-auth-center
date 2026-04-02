package com.keli.authlogin.common.utils;

import java.util.regex.Pattern;

/**
 * 用于验证字符串的格式
 */
public class FormatValidator {
    private FormatValidator() {
        // 工具类私有构造
    }

    /**
     * 中国大陆手机号正则：1开头，第二位3-9，后面9位数字
     */
    private static final Pattern PHONE_PATTERN = Pattern.compile("^1[3-9]\\d{9}$");

    /**
     * 邮箱正则：允许字母数字下划线点横线，@后域名允许字母数字点横线，顶级域至少2位字母
     */
    private static final Pattern EMAIL_PATTERN = Pattern.compile(
            "^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$"
    );

    /**
     * 验证是否为有效手机号（中国大陆）
     * @param phone 手机号字符串
     * @return true=有效，false=无效或空
     */
    public static boolean isPhone(String phone) {
        if (phone == null || phone.isBlank()) {
            return false;
        }
        return PHONE_PATTERN.matcher(phone).matches();
    }

    /**
     * 验证是否为有效邮箱地址
     * @param email 邮箱字符串
     * @return true=有效，false=无效或空
     */
    public static boolean isEmail(String email) {
        if (email == null || email.isBlank()) {
            return false;
        }
        return EMAIL_PATTERN.matcher(email).matches();
    }

    /**
     * 验证是否为手机号或邮箱（至少一种符合）
     * @param input 输入字符串
     * @return true=是手机号或邮箱
     */
    public static boolean isPhoneOrEmail(String input) {
        return isPhone(input) || isEmail(input);
    }
}
