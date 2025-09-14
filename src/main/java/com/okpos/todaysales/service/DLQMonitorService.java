package com.okpos.todaysales.service;

import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.stereotype.Service;
import java.util.Properties;
import java.util.HashMap;
import java.util.Map;

@Service
public class DLQMonitorService {

    private final RabbitAdmin rabbitAdmin;

    public DLQMonitorService(RabbitAdmin rabbitAdmin) {
        this.rabbitAdmin = rabbitAdmin;
    }

    public Map<String, Integer> getDLQMessageCounts() {
        Map<String, Integer> counts = new HashMap<>();

        counts.put("dlq.sales", getQueueMessageCount("dlq.sales"));
        counts.put("dlq.settlement", getQueueMessageCount("dlq.settlement"));
        counts.put("dlq.notification", getQueueMessageCount("dlq.notification"));

        return counts;
    }

    private Integer getQueueMessageCount(String queueName) {
        Properties properties = rabbitAdmin.getQueueProperties(queueName);
        if (properties != null) {
            Object messageCount = properties.get("QUEUE_MESSAGE_COUNT");
            return messageCount != null ? Integer.parseInt(messageCount.toString()) : 0;
        }
        return 0;
    }

    public void purgeDLQ(String queueName) {
        rabbitAdmin.purgeQueue(queueName);
    }
}