package com.example.parkingsystem.util;


import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import java.sql.Connection;
import java.sql.SQLException;

public enum ConnectionUtil {
    INSTANCE;

    private final HikariDataSource dataSource;

    // 공통 사용자 추가
    ConnectionUtil() {
        HikariConfig config = new HikariConfig();
        config.setDriverClassName("org.mariadb.jdbc.Driver");

        // 환경변수 DB_HOST가 있으면 사용, 없으면 localhost (로컬 개발용)
        String dbHost = System.getenv("DB_HOST") != null
                ? System.getenv("DB_HOST")
                : "localhost";

        config.setJdbcUrl("jdbc:mariadb://" + dbHost + ":3306/smart_parking_system");
        config.setUsername("system_user");
        config.setPassword("0220");

        config.addDataSourceProperty("cachePrepStmts", "true");
        config.addDataSourceProperty("prepStmtCacheSize", "250");
        config.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");

        dataSource = new HikariDataSource(config);
    }

    public Connection getConnection() throws SQLException {
        return dataSource.getConnection();
    }
}
