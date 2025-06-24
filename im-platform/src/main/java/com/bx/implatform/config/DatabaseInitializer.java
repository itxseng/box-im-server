package com.bx.implatform.config;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;

@Slf4j
@Component
public class DatabaseInitializer {

    @Value("${spring.datasource.username}")
    private String username;

    @Value("${spring.datasource.password}")
    private String password;

    @Value("${spring.datasource.url}")
    private String fullJdbcUrl;

    private static final String TARGET_DB_NAME = "im_platform";

    @PostConstruct
    public void init() {
        try {
            // 将 spring.datasource.url 去掉数据库名，替换成 mysql 系统库
            String baseUrl = fullJdbcUrl.substring(0, fullJdbcUrl.lastIndexOf("/"));
            String paramPart = "";
            if (fullJdbcUrl.contains("?")) {
                paramPart = fullJdbcUrl.substring(fullJdbcUrl.indexOf("?"));
            }
            String jdbcUrl = baseUrl + "/mysql" + paramPart;

            try (Connection conn = DriverManager.getConnection(jdbcUrl, username, password);
                 Statement stmt = conn.createStatement()) {

                String sql = "CREATE DATABASE IF NOT EXISTS `" + TARGET_DB_NAME + "` " +
                        "DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci";

                stmt.executeUpdate(sql);
                log.info("✅ 数据库 `{}` 已存在或创建成功", TARGET_DB_NAME);
            }
        } catch (Exception e) {
            log.error("❌ 数据库 `{}` 创建失败", TARGET_DB_NAME, e);
        }
    }
}
