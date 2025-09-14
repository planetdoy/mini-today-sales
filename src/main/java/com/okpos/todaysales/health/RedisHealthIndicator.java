package com.okpos.todaysales.health;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.Properties;

@Component("customRedis")
@RequiredArgsConstructor
@Slf4j
public class RedisHealthIndicator implements HealthIndicator {

    private final RedisConnectionFactory redisConnectionFactory;
    private final RedisTemplate<String, Object> redisTemplate;

    @Override
    public Health health() {
        try {
            // Redis 연결 테스트
            RedisConnection connection = redisConnectionFactory.getConnection();

            // PING 명령어로 연결 확인
            String pong = connection.ping();

            // Redis 서버 정보 가져오기
            Properties info = connection.info();

            // 연결 종료
            connection.close();

            // 캐시 테스트 (간단한 set/get)
            String testKey = "health-check-test";
            String testValue = String.valueOf(System.currentTimeMillis());

            redisTemplate.opsForValue().set(testKey, testValue);
            String retrievedValue = (String) redisTemplate.opsForValue().get(testKey);
            redisTemplate.delete(testKey);

            boolean cacheWorking = testValue.equals(retrievedValue);

            return Health.up()
                    .withDetail("status", "Connected")
                    .withDetail("ping", pong)
                    .withDetail("cache_test", cacheWorking ? "PASS" : "FAIL")
                    .withDetail("redis_version", info.getProperty("redis_version"))
                    .withDetail("connected_clients", info.getProperty("connected_clients"))
                    .withDetail("used_memory", info.getProperty("used_memory_human"))
                    .withDetail("uptime", info.getProperty("uptime_in_seconds") + " seconds")
                    .build();

        } catch (Exception e) {
            log.error("Redis Health Check Failed", e);
            return Health.down()
                    .withDetail("status", "Connection Failed")
                    .withDetail("error", e.getMessage())
                    .withException(e)
                    .build();
        }
    }
}