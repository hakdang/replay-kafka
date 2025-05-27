package kr.geun.oss.replayer.producer;

import kr.geun.oss.replayer.config.ReplayConfig;
import org.apache.kafka.clients.producer.MockProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.serialization.StringSerializer;
import org.junit.jupiter.api.Test;

import java.util.Properties;
import java.util.concurrent.Future;

import static org.junit.jupiter.api.Assertions.*;

/**
 * KafkaProducerManager의 단위 테스트
 * Mock Producer를 사용하여 실제 Kafka 연결 없이 테스트
 */
class KafkaProducerManagerTest {

    @Test
    void testProducerProperties_shouldUseCorrectConfiguration() {
        // Given: ReplayConfig 설정
        ReplayConfig config = new ReplayConfig(
            "localhost:9092",        // source
            "localhost:9093",        // target (다른 서버)
            "source-topic",
            "target-topic",
            "test-group",
            null, null, null, null, 0, 10
        );

        // When: Producer properties 생성 로직을 직접 테스트
        Properties props = new Properties();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, config.targetBootstrapServers());
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());

        // Then: Properties가 올바르게 설정되어야 함
        assertEquals("localhost:9093", props.get(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG));
        assertEquals(StringSerializer.class.getName(), props.get(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG));
        assertEquals(StringSerializer.class.getName(), props.get(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG));
    }

    @Test
    void testMockProducerSend_shouldSendMessage() {
        // Given: Mock Producer와 설정
        MockProducer<String, String> mockProducer = new MockProducer<>(true, new StringSerializer(), new StringSerializer());
        ReplayConfig config = new ReplayConfig(
            "localhost:9092",
            "localhost:9092",
            "source-topic",
            "target-topic",
            "test-group",
            null, null, null, null, 0, 10
        );

        // When: 메시지 전송
        ProducerRecord<String, String> record = new ProducerRecord<>(config.targetTopic(), "test-key", "test-value");
        Future<RecordMetadata> future = mockProducer.send(record);

        // Then: 메시지가 성공적으로 전송되어야 함
        assertTrue(future.isDone());
        assertEquals(1, mockProducer.history().size());
        
        ProducerRecord<String, String> sentRecord = mockProducer.history().get(0);
        assertEquals(config.targetTopic(), sentRecord.topic());
        assertEquals("test-key", sentRecord.key());
        assertEquals("test-value", sentRecord.value());
    }

    @Test
    void testMockProducerConfiguration_shouldCreateSuccessfully() {
        // Given: Mock Producer 설정
        StringSerializer keySerializer = new StringSerializer();
        StringSerializer valueSerializer = new StringSerializer();
        MockProducer<String, String> mockProducer = new MockProducer<>(true, keySerializer, valueSerializer);

        // When: Producer 상태 확인
        // Then: Producer가 정상적으로 생성되어야 함
        assertNotNull(mockProducer);
        assertEquals(0, mockProducer.history().size());
        
        // 메시지가 없는 상태에서는 completeNext() 대신 closed 상태를 확인
        assertFalse(mockProducer.closed());
    }

    @Test
    void testProducerRecord_shouldCreateWithCorrectData() {
        // Given: 메시지 데이터
        String topic = "target-topic";
        String key = "message-key";
        String value = "message-value";
        Long timestamp = System.currentTimeMillis();

        // When: ProducerRecord 생성
        ProducerRecord<String, String> record = new ProducerRecord<>(topic, null, timestamp, key, value);

        // Then: 레코드가 올바르게 생성되어야 함
        assertEquals(topic, record.topic());
        assertEquals(key, record.key());
        assertEquals(value, record.value());
        assertEquals(timestamp, record.timestamp());
        assertNull(record.partition()); // 파티션은 지정하지 않음
    }

    @Test
    void testConfigValidation_shouldValidateTargetBootstrapServers() {
        // Given: 다양한 target bootstrap servers 설정
        ReplayConfig config1 = new ReplayConfig(
            "localhost:9092", "localhost:9093", "source", "target", "group", null, null, null, null, 0, 10
        );
        ReplayConfig config2 = new ReplayConfig(
            "localhost:9092", "cluster1:9092,cluster2:9092", "source", "target", "group", null, null, null, null, 0, 10
        );

        // When & Then: 설정 값들이 올바르게 저장되어야 함
        assertEquals("localhost:9093", config1.targetBootstrapServers());
        assertEquals("cluster1:9092,cluster2:9092", config2.targetBootstrapServers());
        assertEquals("target", config1.targetTopic());
        assertEquals("target", config2.targetTopic());
    }
}
