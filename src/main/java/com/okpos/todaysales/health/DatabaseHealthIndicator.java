package com.okpos.todaysales.health;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.Statement;

@Component("customDatabase")
@RequiredArgsConstructor
@Slf4j
public class DatabaseHealthIndicator implements HealthIndicator {

    private final DataSource dataSource;

    @Override
    public Health health() {
        try (Connection connection = dataSource.getConnection()) {

            // 기본 연결 테스트
            if (connection.isValid(5)) {
                DatabaseMetaData metaData = connection.getMetaData();

                // 테이블 개수 확인
                int tableCount = getTableCount(connection);

                // 매출 데이터 개수 확인 (성능 체크)
                long salesCount = getSalesCount(connection);

                return Health.up()
                        .withDetail("status", "Connected")
                        .withDetail("database", metaData.getDatabaseProductName())
                        .withDetail("version", metaData.getDatabaseProductVersion())
                        .withDetail("driver", metaData.getDriverName())
                        .withDetail("driver_version", metaData.getDriverVersion())
                        .withDetail("url", metaData.getURL())
                        .withDetail("tables_count", tableCount)
                        .withDetail("sales_count", salesCount)
                        .withDetail("connection_valid", true)
                        .build();
            } else {
                return Health.down()
                        .withDetail("status", "Connection Invalid")
                        .withDetail("connection_valid", false)
                        .build();
            }

        } catch (Exception e) {
            log.error("Database Health Check Failed", e);
            return Health.down()
                    .withDetail("status", "Connection Failed")
                    .withDetail("error", e.getMessage())
                    .withException(e)
                    .build();
        }
    }

    private int getTableCount(Connection connection) {
        try (Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery("SELECT COUNT(*) FROM information_schema.tables WHERE table_schema = DATABASE()")) {

            if (resultSet.next()) {
                return resultSet.getInt(1);
            }
        } catch (Exception e) {
            log.warn("Failed to get table count", e);
        }
        return -1;
    }

    private long getSalesCount(Connection connection) {
        try (Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery("SELECT COUNT(*) FROM sales")) {

            if (resultSet.next()) {
                return resultSet.getLong(1);
            }
        } catch (Exception e) {
            log.warn("Failed to get sales count", e);
        }
        return -1;
    }
}