package com.bx.implatform.util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 正则校验
 * @author Blue
 * @version 1.0
 * @date 2025-03-21
 */
public class RegexUtil {

    // 国内手机号（11位）
    private static final Pattern CN_PHONE_REGEX = Pattern.compile("^1[3-9]\\d{9}$");

    // 国际手机号（E.164 标准）
    private static final Pattern INTL_PHONE_REGEX = Pattern.compile("^\\+?[1-9]\\d{1,14}$");

    private static final Pattern EMAIL_REGEX = Pattern.compile("^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$");

    /**
     * 判断是否是中国手机号
     * @param text
     * @return
     */
    public static boolean isCnPhone(String text) {
        return text != null && CN_PHONE_REGEX.matcher(text).matches();
    }

    /**
     * 检测是否是手机号  国际号码
     * @param text
     * @return
     */
    public static boolean isPhone(String text) {
        return text != null && INTL_PHONE_REGEX.matcher(text).matches();
    }

    /**
     * 检测是否是邮箱
     * @param text
     * @return
     */
    public static boolean isEmail(String text) {
        return text != null && EMAIL_REGEX.matcher(text).matches();
    }
}
