package com.okpos.todaysales.integration;

import com.redis.testcontainers.RedisContainer;
import org.junit.jupiter.api.TestInstance;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureWebMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.TestPropertySource;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.containers.RabbitMQContainer;
import org.testcontainers.utility.DockerImageName;

/**
 * 통합 테스트를 위한 기본 설정 클래스
 * TestContainers를 사용하여 MySQL, Redis, RabbitMQ를 구동
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@TestPropertySource(properties = {
    "spring.jpa.hibernate.ddl-auto=create-drop",
    "spring.jpa.show-sql=true",
    "logging.level.org.springframework.web=DEBUG",
    "logging.level.org.springframework.amqp=DEBUG",
    "management.endpoints.web.exposure.include=*"
})
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public abstract class BaseIntegrationTest {

    /**
     * MySQL TestContainer 설정 - 싱글톤 패턴으로 컨테이너 재사용
     */
    protected static final MySQLContainer<?> mysqlContainer;

    /**
     * Redis TestContainer 설정
     */
    protected static final RedisContainer redisContainer;

    /**
     * RabbitMQ TestContainer 설정
     */
    protected static final RabbitMQContainer rabbitMQContainer;

    // 정적 초기화 블록에서 컨테이너 시작
    static {
        mysqlContainer = new MySQLContainer<>(DockerImageName.parse("mysql:8.0"))
                .withDatabaseName("today_sales_test")
                .withUsername("test")
                .withPassword("test")
                .withReuse(true);
        mysqlContainer.start();

        redisContainer = new RedisContainer(DockerImageName.parse("redis:7-alpine"))
                .withReuse(true);
        redisContainer.start();

        rabbitMQContainer = new RabbitMQContainer(DockerImageName.parse("rabbitmq:3-management"))
                .withReuse(true);
        rabbitMQContainer.start();
    }

    /**
     * TestContainers의 동적 포트를 Spring 설정에 주입
     */
    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        // MySQL 설정
        registry.add("spring.datasource.url", mysqlContainer::getJdbcUrl);
        registry.add("spring.datasource.username", mysqlContainer::getUsername);
        registry.add("spring.datasource.password", mysqlContainer::getPassword);
        registry.add("spring.datasource.driver-class-name", () -> "com.mysql.cj.jdbc.Driver");

        // Redis 설정
        registry.add("spring.redis.host", redisContainer::getHost);
        registry.add("spring.redis.port", () -> redisContainer.getMappedPort(6379));

        // RabbitMQ 설정
        registry.add("spring.rabbitmq.host", rabbitMQContainer::getHost);
        registry.add("spring.rabbitmq.port", () -> rabbitMQContainer.getMappedPort(5672));
        registry.add("spring.rabbitmq.username", () -> "guest");
        registry.add("spring.rabbitmq.password", () -> "guest");

        // Management port for RabbitMQ
        registry.add("spring.rabbitmq.management.port", () -> rabbitMQContainer.getMappedPort(15672));
    }

    /**
     * 테스트 데이터 정리를 위한 헬퍼 메서드
     */
    protected void cleanupTestData() {
        // 각 테스트 후 데이터 정리 로직 구현 가능
    }

    /**
     * 컨테이너 상태 확인 메서드
     */
    protected void verifyContainersRunning() {
        assert mysqlContainer.isRunning() : "MySQL container should be running";
        assert redisContainer.isRunning() : "Redis container should be running";
        assert rabbitMQContainer.isRunning() : "RabbitMQ container should be running";
    }
}