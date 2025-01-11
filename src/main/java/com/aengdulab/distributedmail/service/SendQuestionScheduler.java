package com.aengdulab.distributedmail.service;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;
import com.aengdulab.distributedmail.DistributedLock;
import com.aengdulab.distributedmail.DistributedSupport;
import com.aengdulab.distributedmail.FollowerTask;
import com.aengdulab.distributedmail.LeaderTask;
import com.aengdulab.distributedmail.domain.Subscribe;
import com.aengdulab.distributedmail.domain.SubscribeQuestionMessage;
import com.aengdulab.distributedmail.repository.SubscribeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@RequiredArgsConstructor
@Component
public class SendQuestionScheduler {

    private final QuestionSender questionSender;
    private final SubscribeRepository subscribeRepository;
    private final DistributedLock distributedLock;
    private final DataSource dataSource;
    private final FollowerTask followerTask;
    private final LeaderTask leaderTask;
    private final DistributedSupport distributedSupport;

    @Transactional
    @Scheduled(cron = "0 0 9 * * *", zone = "Asia/Seoul")
    public void sendQuestion() {
        Connection connection;
        try {
            connection = dataSource.getConnection();
            boolean isLeaderNode = distributedLock.tryLock(connection, "leader", 0);
            detectNodes(isLeaderNode);
            releaseLock(connection, isLeaderNode);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        } finally {
            log.info("distributed support index = {} count = {}", distributedSupport.getIndex(), distributedSupport.getCount());
            List<Subscribe> subscribes = subscribeRepository.findAll();
            sendQuestionMails(subscribes);
        }
    }

    private void detectNodes(boolean isLeaderNode) {
        if (isLeaderNode) {
            leaderTask.start();
            return;
        }

        followerTask.start();
    }

    private void releaseLock(Connection connection, boolean isLeaderNode) {
        if (isLeaderNode) {
            distributedLock.releaseLock(connection, "leader");
        }
    }

    private void sendQuestionMails(List<Subscribe> subscribes) {
        subscribes.stream()
                .filter(subscribe -> distributedSupport.isMine(subscribe.getId()))
                .flatMap(subscribe -> choiceQuestion(subscribe).stream())
                .forEach(questionSender::sendQuestion);
    }

    private Optional<SubscribeQuestionMessage> choiceQuestion(Subscribe subscribe) {
        return Optional.ofNullable(subscribe.getQuestion())
                .map(nextQuestion -> new SubscribeQuestionMessage(subscribe, nextQuestion))
                .or(() -> {
                    log.error("[질문 조회 실패] subscribeId = {}", subscribe.getId());
                    return Optional.empty();
                });
    }
}
