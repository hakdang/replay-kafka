package kr.hakdang.oss.replayer.consumer;

import kr.hakdang.oss.replayer.config.ReplayConfig;
import org.apache.kafka.clients.consumer.*;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * Kafka Consumer를 생성하고 설정하는 역할
 */
public class KafkaConsumerManager {
    private static final Logger logger = LoggerFactory.getLogger(KafkaConsumerManager.class);

    public static KafkaConsumer<String, String> createConsumer(ReplayConfig config) {
        var props = buildConsumerProperties(config);
        return new KafkaConsumer<>(props);
    }

    private static Properties buildConsumerProperties(ReplayConfig config) {
        var props = new Properties();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, config.sourceBootstrapServers());
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, false);

        // Subscribe 모드일 때만 group.id와 auto.offset.reset 설정
        if (config.isSubscribeMode()) {
            props.put(ConsumerConfig.GROUP_ID_CONFIG, config.groupId());
            props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        }

        return props;
    }

    public static void setupConsumer(KafkaConsumer<String, String> consumer, ReplayConfig config) {
        if (config.isSubscribeMode()) {
            setupSubscribeMode(consumer, config);
        } else {
            setupAssignMode(consumer, config);
        }
    }

    private static void setupSubscribeMode(KafkaConsumer<String, String> consumer, ReplayConfig config) {
        consumer.subscribe(List.of(config.sourceTopic()));
        logger.info("Consumer subscribed to topic {}", config.sourceTopic());
    }

    private static void setupAssignMode(KafkaConsumer<String, String> consumer, ReplayConfig config) {
        var topicPartition = new TopicPartition(config.sourceTopic(), config.partition());
        consumer.assign(List.of(topicPartition));

        switch (determinePositionStrategy(config)) {
            case OFFSET -> seekToOffset(consumer, topicPartition, config);
            case TIMESTAMP -> seekToTimestamp(consumer, topicPartition, config);
            case DEFAULT -> logger.info("Consumer assigned to partition {} with default position", config.partition());
        }
    }

    private static PositionStrategy determinePositionStrategy(ReplayConfig config) {
        if (config.isOffsetBased()) return PositionStrategy.OFFSET;
        if (config.isTimestampBased()) return PositionStrategy.TIMESTAMP;
        return PositionStrategy.DEFAULT;
    }

    private static void seekToOffset(KafkaConsumer<String, String> consumer, TopicPartition tp, ReplayConfig config) {
        consumer.seek(tp, config.offset());
        logger.info("Consumer positioned at partition {} offset {}", config.partition(), config.offset());
    }

    private static void seekToTimestamp(KafkaConsumer<String, String> consumer, TopicPartition tp, ReplayConfig config) {
        var timestampsToSearch = Map.of(tp, config.timestamp().toEpochMilli());
        var offsets = consumer.offsetsForTimes(timestampsToSearch);

        Optional.ofNullable(offsets.get(tp))
            .ifPresentOrElse(
                offsetAndTimestamp -> {
                    consumer.seek(tp, offsetAndTimestamp.offset());
                    logger.info("Consumer positioned at partition {} timestamp {} (offset {})",
                        config.partition(), config.timestamp(), offsetAndTimestamp.offset());
                },
                () -> logger.warn("No messages found after timestamp {}", config.timestamp())
            );
    }

    private enum PositionStrategy {
        OFFSET, TIMESTAMP, DEFAULT
    }
}
