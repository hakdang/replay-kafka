package kr.hakdang.oss.replayer.config;

import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;

/**
 * ReplayConfig의 단위 테스트
 */
class ReplayConfigTest {

    @Test
    void testReplayConfig_withValidValues_shouldCreateSuccessfully() {
        // Given: 유효한 설정 값들
        String sourceBootstrapServers = "localhost:9092";
        String targetBootstrapServers = "localhost:9093";
        String sourceTopic = "source-topic";
        String targetTopic = "target-topic";
        String groupId = "test-group";

        // When: ReplayConfig 생성
        ReplayConfig config = new ReplayConfig(
            sourceBootstrapServers, targetBootstrapServers,
            sourceTopic, targetTopic, groupId,
            null, null, null, null, 0, 10
        );

        // Then: 값이 올바르게 설정되었는지 확인
        assertEquals(sourceBootstrapServers, config.sourceBootstrapServers());
        assertEquals(targetBootstrapServers, config.targetBootstrapServers());
        assertEquals(sourceTopic, config.sourceTopic());
        assertEquals(targetTopic, config.targetTopic());
        assertEquals(groupId, config.groupId());
    }

    @Test
    void testReplayConfig_withNullSourceBootstrapServers_shouldThrowException() {
        // When & Then: null source bootstrap servers로 생성 시 예외 발생
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> new ReplayConfig(
                null, "localhost:9093",
                "source-topic", "target-topic", "test-group",
                null, null, null, null, 0, 10
            )
        );

        assertEquals("kafka.source.bootstrap.servers is required", exception.getMessage());
    }

    @Test
    void testReplayConfig_withBlankSourceBootstrapServers_shouldThrowException() {
        // When & Then: 빈 source bootstrap servers로 생성 시 예외 발생
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> new ReplayConfig(
                "   ", "localhost:9093",
                "source-topic", "target-topic", "test-group",
                null, null, null, null, 0, 10
            )
        );

        assertEquals("kafka.source.bootstrap.servers is required", exception.getMessage());
    }

    @Test
    void testReplayConfig_withNullTargetBootstrapServers_shouldThrowException() {
        // When & Then: null target bootstrap servers로 생성 시 예외 발생
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> new ReplayConfig(
                "localhost:9092", null,
                "source-topic", "target-topic", "test-group",
                null, null, null, null, 0, 10
            )
        );

        assertEquals("kafka.target.bootstrap.servers is required", exception.getMessage());
    }

    @Test
    void testReplayConfig_withBlankTargetBootstrapServers_shouldThrowException() {
        // When & Then: 빈 target bootstrap servers로 생성 시 예외 발생
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> new ReplayConfig(
                "localhost:9092", "",
                "source-topic", "target-topic", "test-group",
                null, null, null, null, 0, 10
            )
        );

        assertEquals("kafka.target.bootstrap.servers is required", exception.getMessage());
    }

    @Test
    void testReplayConfig_withNullSourceTopic_shouldThrowException() {
        // When & Then: null source topic으로 생성 시 예외 발생
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> new ReplayConfig(
                "localhost:9092", "localhost:9093",
                null, "target-topic", "test-group",
                null, null, null, null, 0, 10
            )
        );

        assertEquals("kafka.source.topic is required", exception.getMessage());
    }

    @Test
    void testReplayConfig_withNullTargetTopic_shouldThrowException() {
        // When & Then: null target topic으로 생성 시 예외 발생
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> new ReplayConfig(
                "localhost:9092", "localhost:9093",
                "source-topic", null, "test-group",
                null, null, null, null, 0, 10
            )
        );

        assertEquals("kafka.target.topic is required", exception.getMessage());
    }

    @Test
    void testIsSubscribeMode_withNullPartition_shouldReturnTrue() {
        // Given: partition이 null인 설정
        ReplayConfig config = new ReplayConfig(
            "localhost:9092", "localhost:9093",
            "source-topic", "target-topic", "test-group",
            null, null, null, null, 0, 10
        );

        // When & Then: subscribe 모드여야 함
        assertTrue(config.isSubscribeMode());
    }

    @Test
    void testIsSubscribeMode_withPartition_shouldReturnFalse() {
        // Given: partition이 설정된 설정
        ReplayConfig config = new ReplayConfig(
            "localhost:9092", "localhost:9093",
            "source-topic", "target-topic", "test-group",
            0, null, null, null, 0, 10
        );

        // When & Then: subscribe 모드가 아니어야 함
        assertFalse(config.isSubscribeMode());
    }

    @Test
    void testIsOffsetBased_withOffset_shouldReturnTrue() {
        // Given: offset이 설정된 설정
        ReplayConfig config = new ReplayConfig(
            "localhost:9092", "localhost:9093",
            "source-topic", "target-topic", "test-group",
            null, 100L, null, null, 0, 10
        );

        // When & Then: offset 기반이어야 함
        assertTrue(config.isOffsetBased());
    }

    @Test
    void testIsOffsetBased_withNullOffset_shouldReturnFalse() {
        // Given: offset이 null인 설정
        ReplayConfig config = new ReplayConfig(
            "localhost:9092", "localhost:9093",
            "source-topic", "target-topic", "test-group",
            null, null, null, null, 0, 10
        );

        // When & Then: offset 기반이 아니어야 함
        assertFalse(config.isOffsetBased());
    }

    @Test
    void testIsTimestampBased_withTimestamp_shouldReturnTrue() {
        // Given: timestamp가 설정된 설정
        ReplayConfig config = new ReplayConfig(
            "localhost:9092", "localhost:9093",
            "source-topic", "target-topic", "test-group",
            null, null, Instant.now(), null, 0, 10
        );

        // When & Then: timestamp 기반이어야 함
        assertTrue(config.isTimestampBased());
    }

    @Test
    void testIsTimestampBased_withNullTimestamp_shouldReturnFalse() {
        // Given: timestamp가 null인 설정
        ReplayConfig config = new ReplayConfig(
            "localhost:9092", "localhost:9093",
            "source-topic", "target-topic", "test-group",
            null, null, null, null, 0, 10
        );

        // When & Then: timestamp 기반이 아니어야 함
        assertFalse(config.isTimestampBased());
    }

    @Test
    void testHasMaxMessageLimit_withMaxMessages_shouldReturnTrue() {
        // Given: maxMessages가 설정된 설정
        ReplayConfig config = new ReplayConfig(
            "localhost:9092", "localhost:9093",
            "source-topic", "target-topic", "test-group",
            null, null, null, 1000, 0, 10
        );

        // When & Then: 최대 메시지 제한이 있어야 함
        assertTrue(config.hasMaxMessageLimit());
    }

    @Test
    void testHasMaxMessageLimit_withNullMaxMessages_shouldReturnFalse() {
        // Given: maxMessages가 null인 설정
        ReplayConfig config = new ReplayConfig(
            "localhost:9092", "localhost:9093",
            "source-topic", "target-topic", "test-group",
            null, null, null, null, 0, 10
        );

        // When & Then: 최대 메시지 제한이 없어야 함
        assertFalse(config.hasMaxMessageLimit());
    }

    @Test
    void testHasMaxMessageLimit_withZeroMaxMessages_shouldReturnFalse() {
        // Given: maxMessages가 0인 설정
        ReplayConfig config = new ReplayConfig(
            "localhost:9092", "localhost:9093",
            "source-topic", "target-topic", "test-group",
            null, null, null, 0, 0, 10
        );

        // When & Then: 최대 메시지 제한이 없어야 함
        assertFalse(config.hasMaxMessageLimit());
    }

    @Test
    void testHasProcessingDelay_withDelay_shouldReturnTrue() {
        // Given: processingDelay가 설정된 설정
        ReplayConfig config = new ReplayConfig(
            "localhost:9092", "localhost:9093",
            "source-topic", "target-topic", "test-group",
            null, null, null, null, 100, 10
        );

        // When & Then: 처리 지연이 있어야 함
        assertTrue(config.hasProcessingDelay());
    }

    @Test
    void testHasProcessingDelay_withZeroDelay_shouldReturnFalse() {
        // Given: processingDelay가 0인 설정
        ReplayConfig config = new ReplayConfig(
            "localhost:9092", "localhost:9093",
            "source-topic", "target-topic", "test-group",
            null, null, null, null, 0, 10
        );

        // When & Then: 처리 지연이 없어야 함
        assertFalse(config.hasProcessingDelay());
    }
}
