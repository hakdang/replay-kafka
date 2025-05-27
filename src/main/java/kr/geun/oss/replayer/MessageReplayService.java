package kr.geun.oss.replayer;

import kr.geun.oss.replayer.config.ReplayConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.concurrent.ExecutionException;

/**
 * 메시지 리플레이를 수행하는 핵심 로직
 */
public class MessageReplayService {
  private static final Logger logger = LoggerFactory.getLogger(MessageReplayService.class);

  private final KafkaConsumer<String, String> consumer;
  private final KafkaProducer<String, String> producer;
  private final ReplayConfig config;
  private final ReplayStatistics statistics;

  public MessageReplayService(
    KafkaConsumer<String, String> consumer,
    KafkaProducer<String, String> producer,
    ReplayConfig config
  ) {
    this.consumer = consumer;
    this.producer = producer;
    this.config = config;
    this.statistics = new ReplayStatistics();
  }

  public void startReplay() {
    logger.info("Starting message replay from {} to {}", config.sourceTopic(), config.targetTopic());

    while (!shouldStopReplay()) {
      var records = consumer.poll(Duration.ofSeconds(1));

      if (records.isEmpty()) {
        statistics.incrementEmptyPollCount();
        continue;
      }

      statistics.resetEmptyPollCount();
      processRecords(records);
    }

    logCompletionMessage();
  }

  private boolean shouldStopReplay() {
    return statistics.shouldTimeout(config.consumerTimeout()) ||
      statistics.hasReachedMaxMessages(config.maxMessages());
  }

  private void processRecords(ConsumerRecords<String, String> records) {
    for (var record : records) {
      if (statistics.hasReachedMaxMessages(config.maxMessages())) {
        logger.info("Reached maximum message limit: {}", config.maxMessages());
        return;
      }

      replayMessage(record);
      statistics.incrementMessageCount();
      applyProcessingDelay();
    }
  }

  private void replayMessage(ConsumerRecord<String, String> record) {
    var producerRecord = new ProducerRecord<>(
      config.targetTopic(),
      record.key(),
      record.value()
    );

    try {
      producer.send(producerRecord).get();
      logReplayedMessage(record);
    } catch (InterruptedException | ExecutionException e) {
      logger.error("Failed to send message", e);
    }
  }

  private void logReplayedMessage(ConsumerRecord<String, String> record) {
    logger.info("Replayed message {}: partition={}, offset={}, key={}",
      statistics.getTotalMessages(), record.partition(), record.offset(), record.key());
  }

  private void applyProcessingDelay() {
    if (config.hasProcessingDelay()) {
      try {
        Thread.sleep(config.processingDelay());
      } catch (InterruptedException e) {
        Thread.currentThread().interrupt();
        logger.warn("Processing delay interrupted", e);
      }
    }
  }

  private void logCompletionMessage() {
    if (statistics.shouldTimeout(config.consumerTimeout())) {
      logger.info("No more messages after {} seconds timeout", config.consumerTimeout());
    }
    logger.info("Replay completed. Total messages replayed: {}", statistics.getTotalMessages());
  }

  /**
   * 리플레이 통계를 관리하는 내부 클래스
   */
  private static class ReplayStatistics {
    private int totalMessages = 0;
    private int emptyPollCount = 0;

    void incrementMessageCount() {
      totalMessages++;
    }

    void incrementEmptyPollCount() {
      emptyPollCount++;
    }

    void resetEmptyPollCount() {
      emptyPollCount = 0;
    }

    boolean shouldTimeout(int maxEmptyPolls) {
      return emptyPollCount >= maxEmptyPolls;
    }

    boolean hasReachedMaxMessages(Integer maxMessages) {
      return maxMessages != null && totalMessages >= maxMessages;
    }

    int getTotalMessages() {
      return totalMessages;
    }
  }
}
