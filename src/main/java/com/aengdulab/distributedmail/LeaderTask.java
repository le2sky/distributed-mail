package com.aengdulab.distributedmail;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class LeaderTask {

    private final DistributedSupport distributedSupport;
    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final List<FollowerMessage> messages = Collections.synchronizedList(new ArrayList<>());

    public void start() {
        log.info("leaderTask started");
        sleep(5000);

        distributedSupport.setIndex(1);
        distributedSupport.setCount(messages.size() + 1);
        for (int i = 0; i < messages.size(); i++) {
            LeaderMessage leaderMessage = new LeaderMessage(messages.get(i).address(), messages.size() + 1, i + 2);
            kafkaTemplate.send("leader", leaderMessage);
        }
    }

    private void sleep(int millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    @KafkaListener(topics = "loyalty", groupId = "consumerGroup-" + "#{T(java.util.UUID).randomUUID()})")
    public void receiveMessage(FollowerMessage followerMessage) {
        messages.add(followerMessage);
    }
}
