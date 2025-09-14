package com.okpos.todaysales.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;

@Configuration
public class SchedulerConfig {

    /**
     * 스케줄러 전용 스레드 풀 설정
     */
    @Bean
    public TaskScheduler taskScheduler() {
        ThreadPoolTaskScheduler scheduler = new ThreadPoolTaskScheduler();

        // 기본 스레드 수 (동시 실행 가능한 스케줄 작업 수)
        scheduler.setPoolSize(5);

        // 스레드 이름 접두사
        scheduler.setThreadNamePrefix("settlement-scheduler-");

        // 애플리케이션 종료 시 실행 중인 작업 완료 대기
        scheduler.setWaitForTasksToCompleteOnShutdown(true);

        // 최대 대기 시간 (30초)
        scheduler.setAwaitTerminationSeconds(30);

        // 스레드 풀 초기화
        scheduler.initialize();

        return scheduler;
    }
}