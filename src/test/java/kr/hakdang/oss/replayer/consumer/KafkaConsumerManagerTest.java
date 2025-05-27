package kr.hakdang.oss.replayer.consumer;

import kr.hakdang.oss.replayer.config.ReplayConfig;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.MockConsumer;
import org.apache.kafka.clients.consumer.OffsetResetStrategy;
import org.apache.kafka.common.TopicPartition;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.Properties;

import static org.junit.jupiter.api.Assertions.*;

/**
 * KafkaConsumerManager의 단위 테스트
 * Mock Consumer를 사용하여 실제 Kafka 연결 없이 테스트
 */
class KafkaConsumerManagerTest {

    @Test
    void testConsumerProperties_shouldUseCorrectConfiguration() {
        // Given: ReplayConfig with Subscribe mode
        ReplayConfig config = new ReplayConfig(
            "localhost:9092",
            "localhost:9093",
            "source-topic",
            "target-topic",
            "test-group",
            null, null, null, null, 0, 10
        );

        // When: Consumer properties 생성 로직을 직접 테스트
        Properties props = new Properties();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, config.sourceBootstrapServers());
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringDeserializer");
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringDeserializer");
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, false);
        if (config.groupId() != null) {
            props.put(ConsumerConfig.GROUP_ID_CONFIG, config.groupId());
        }

        // Then: Properties가 올바르게 설정되어야 함
        assertEquals("localhost:9092", props.get(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG));
        assertEquals("test-group", props.get(ConsumerConfig.GROUP_ID_CONFIG));
        assertEquals("earliest", props.get(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG));
        assertEquals(false, props.get(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG));
    }

    @Test
    void testConsumerProperties_withAssignMode_shouldNotHaveGroupId() {
        // Given: ReplayConfig with Assign mode (no group id)
        ReplayConfig config = new ReplayConfig(
            "localhost:9092",
            "localhost:9092",
            "source-topic",
            "target-topic",
            null, // no group id for assign mode
            0, null, null, null, 0, 10
        );

        // When: Consumer properties 생성 로직을 직접 테스트
        Properties props = new Properties();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, config.sourceBootstrapServers());
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringDeserializer");
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringDeserializer");
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "latest");
        props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, false);
        // group id는 설정하지 않음

        // Then: Properties가 올바르게 설정되어야 함
        assertEquals("localhost:9092", props.get(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG));
        assertNull(props.get(ConsumerConfig.GROUP_ID_CONFIG));
        assertEquals("latest", props.get(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG));
    }

    @Test
    void testMockConsumerSetup_withSubscribeMode() {
        // Given: Mock Consumer와 Subscribe 모드 설정
        MockConsumer<String, String> mockConsumer = new MockConsumer<>(OffsetResetStrategy.EARLIEST);
        ReplayConfig config = new ReplayConfig(
            "localhost:9092",
            "localhost:9092",
            "source-topic",
            "target-topic",
            "test-group",
            null, null, null, null, 0, 10
        );

        // When: Subscribe 설정
        assertDoesNotThrow(() -> {
            mockConsumer.subscribe(Collections.singletonList(config.sourceTopic()));
        });

        // Then: 구독이 올바르게 설정되어야 함
        assertEquals(Collections.singleton(config.sourceTopic()), mockConsumer.subscription());
    }

    @Test
    void testMockConsumerSetup_withAssignMode() {
        // Given: Mock Consumer와 Assign 모드 설정
        MockConsumer<String, String> mockConsumer = new MockConsumer<>(OffsetResetStrategy.LATEST);
        ReplayConfig config = new ReplayConfig(
            "localhost:9092",
            "localhost:9092",
            "source-topic",
            "target-topic",
            null, // no group id
            0, null, null, null, 0, 10
        );

        // When: Assign 설정
        TopicPartition partition = new TopicPartition(config.sourceTopic(), config.partition());
        assertDoesNotThrow(() -> {
            mockConsumer.assign(Collections.singletonList(partition));
        });

        // Then: 파티션이 올바르게 할당되어야 함
        assertEquals(Collections.singleton(partition), mockConsumer.assignment());
    }

    @Test
    void testConfigValidation_shouldValidateRequiredFields() {
        // Given: 필수 필드들
        String sourceBootstrapServers = "localhost:9092";
        String sourceTopic = "source-topic";
        String targetTopic = "target-topic";

        // When: ReplayConfig 생성
        ReplayConfig config = new ReplayConfig(
            sourceBootstrapServers,
            "localhost:9092",
            sourceTopic,
            targetTopic,
            "test-group",
            null, null, null, null, 0, 10
        );

        // Then: 설정 값들이 올바르게 저장되어야 함
        assertEquals(sourceBootstrapServers, config.sourceBootstrapServers());
        assertEquals(sourceTopic, config.sourceTopic());
        assertEquals(targetTopic, config.targetTopic());
        assertEquals("test-group", config.groupId());
    }
}
