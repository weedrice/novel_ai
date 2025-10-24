package com.jwyoo.api.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationEnvironmentPreparedEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;

/**
 * 애플리케이션 시작 시 PostgreSQL 데이터베이스가 없으면 자동으로 생성합니다.
 * ApplicationEnvironmentPreparedEvent를 사용하여 DataSource 초기화 전에 실행됩니다.
 */
@Slf4j
@Component
public class DatabaseInitializer implements ApplicationListener<ApplicationEnvironmentPreparedEvent> {

    @Override
    public void onApplicationEvent(ApplicationEnvironmentPreparedEvent event) {
        Environment env = event.getEnvironment();

        String datasourceUrl = env.getProperty("spring.datasource.url");
        String username = env.getProperty("spring.datasource.username");
        String password = env.getProperty("spring.datasource.password");
        String databaseName = env.getProperty("app.database.name", "novel_ai");

        if (datasourceUrl == null || !datasourceUrl.contains("postgresql")) {
            log.debug("PostgreSQL이 아니거나 datasource URL이 설정되지 않았습니다. DB 자동 생성을 건너뜁니다.");
            return;
        }

        initializeDatabase(datasourceUrl, username, password, databaseName);
    }

    private void initializeDatabase(String datasourceUrl, String username, String password, String databaseName) {
        try {
            // JDBC URL에서 호스트와 포트 추출
            String baseUrl = extractBaseUrl(datasourceUrl);

            log.info("데이터베이스 초기화 시작: {}", databaseName);

            // postgres 기본 데이터베이스에 연결
            String postgresUrl = baseUrl + "/postgres";

            try (Connection conn = DriverManager.getConnection(postgresUrl, username, password);
                 Statement stmt = conn.createStatement()) {

                // 데이터베이스 존재 여부 확인
                String checkDbSql = String.format(
                    "SELECT 1 FROM pg_database WHERE datname = '%s'", databaseName
                );

                ResultSet rs = stmt.executeQuery(checkDbSql);

                if (!rs.next()) {
                    // 데이터베이스가 없으면 생성
                    log.info("데이터베이스 '{}' 가 존재하지 않습니다. 생성합니다...", databaseName);
                    String createDbSql = String.format("CREATE DATABASE %s", databaseName);
                    stmt.executeUpdate(createDbSql);
                    log.info("데이터베이스 '{}' 생성 완료", databaseName);
                } else {
                    log.info("데이터베이스 '{}' 가 이미 존재합니다.", databaseName);
                }

                rs.close();
            }

        } catch (Exception e) {
            log.error("데이터베이스 초기화 실패: {}", e.getMessage(), e);
            // 데이터베이스 생성 실패해도 애플리케이션은 계속 진행
            // (이미 DB가 있을 수 있음)
        }
    }

    /**
     * JDBC URL에서 기본 URL 추출
     * 예: jdbc:postgresql://localhost:5432/novel_ai -> jdbc:postgresql://localhost:5432
     */
    private String extractBaseUrl(String url) {
        // jdbc:postgresql://host:port/database 형식에서 database 부분 제거
        int lastSlash = url.lastIndexOf('/');
        if (lastSlash > 0) {
            return url.substring(0, lastSlash);
        }
        return url;
    }
}
