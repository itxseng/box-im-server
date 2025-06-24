package com.bx.implatform.util;

import jakarta.servlet.http.HttpServletRequest;

public class IpUtils {

    public static String getClientIp(HttpServletRequest request) {
        String[] headers = {
            "X-Forwarded-For",
            "X-Real-IP",
            "Proxy-Client-IP",
            "WL-Proxy-Client-IP"
        };
        for (String header : headers) {
            String ip = request.getHeader(header);
            if (ip != null && ip.length() != 0 && !"unknown".equalsIgnoreCase(ip)) {
                return ip.split(",")[0].trim();  // 防止多个 IP 取第一个
            }
        }
        return request.getRemoteAddr();
    }
}
