# Java 11 기반 OpenJDK 이미지
FROM openjdk:11-jre-slim

# 작업 디렉토리 설정
WORKDIR /app

# Maven으로 빌드된 JAR 파일 복사
COPY target/mini-today-sales-0.0.1-SNAPSHOT.jar app.jar

# 애플리케이션 포트 노출
EXPOSE 8080

# 애플리케이션 실행
ENTRYPOINT ["java", "-jar", "app.jar"]

# Health check 추가
HEALTHCHECK --interval=30s --timeout=3s --start-period=40s --retries=3 \
  CMD curl -f http://localhost:8080/actuator/health || exit 1