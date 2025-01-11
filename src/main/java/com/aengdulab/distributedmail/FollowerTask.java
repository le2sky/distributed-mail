package com.aengdulab.distributedmail;

import java.net.InetAddress;
import java.net.UnknownHostException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationContext;
import org.springframework.core.env.Environment;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class FollowerTask {

    private final ApplicationContext applicationContext;
    private final GlobalLatch globalLatch;
    private final DistributedSupport distributedSupport;
    private final KafkaTemplate<String, Object> kafkaTemplate;

    public void start() {
        log.info("clientTask started");
        String address = createAddress();
        FollowerMessage message = new FollowerMessage(address);
        kafkaTemplate.send("loyalty", message);
        globalLatch.await();
    }

    @KafkaListener(topics = "leader", groupId = "consumerGroup-" + "#{T(java.util.UUID).randomUUID()})")
    public void receiveMessage(LeaderMessage leaderMessage) {
        String address = createAddress();
        if (address.equals(leaderMessage.address())) {
            distributedSupport.setIndex(leaderMessage.index());
            distributedSupport.setCount(leaderMessage.total());
            globalLatch.countDown();
        }
    }

    private String createAddress() {
        try {
            String ip = InetAddress.getLocalHost().getHostAddress();
            int port = applicationContext.getBean(Environment.class).getProperty("server.port", Integer.class, 8080);
            return ip + ":" + port;
        } catch (UnknownHostException e) {
            throw new RuntimeException(e);
        }
    }
}
