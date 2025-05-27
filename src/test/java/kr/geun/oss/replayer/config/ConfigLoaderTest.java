package kr.geun.oss.replayer.config;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;

/**
 * ConfigLoader의 단위 테스트
 */
class ConfigLoaderTest {

    @TempDir
    Path tempDir;
    
    private Path configFile;

    @BeforeEach
    void setUp() {
        configFile = tempDir.resolve("test-application.properties");
    }

    @Test
    void testLoadConfig_withValidProperties_shouldCreateReplayConfig() throws IOException {
        // Given: 유효한 설정 파일 생성
        String configContent = """
            kafka.source.bootstrap.servers=localhost:9092
            kafka.target.bootstrap.servers=localhost:9093
            kafka.source.topic=source-topic
            kafka.target.topic=target-topic
            kafka.group.id=test-group
            kafka.partition=0
            kafka.offset=100
            kafka.timestamp=2024-01-01T00:00:00Z
            kafka.max.messages=1000
            kafka.processing.delay=100
            kafka.consumer.timeout=30
            """;
        
        writeConfigFile(configContent);

        // When: 설정 로드
        ReplayConfig config = ConfigLoader.loadConfig(configFile.toString());

        // Then: 설정이 올바르게 로드되었는지 확인
        assertEquals("localhost:9092", config.sourceBootstrapServers());
        assertEquals("localhost:9093", config.targetBootstrapServers());
        assertEquals("source-topic", config.sourceTopic());
        assertEquals("target-topic", config.targetTopic());
        assertEquals("test-group", config.groupId());
        assertEquals(0, config.partition());
        assertEquals(100L, config.offset());
        assertEquals(Instant.parse("2024-01-01T00:00:00Z"), config.timestamp());
        assertEquals(1000, config.maxMessages());
        assertEquals(100L, config.processingDelay());
        assertEquals(30, config.consumerTimeout());
    }

    @Test
    void testLoadConfig_withMinimalProperties_shouldUseDefaults() throws IOException {
        // Given: 최소한의 필수 설정만 있는 파일
        String configContent = """
            kafka.source.bootstrap.servers=localhost:9092
            kafka.target.bootstrap.servers=localhost:9093
            kafka.source.topic=source-topic
            kafka.target.topic=target-topic
            """;
        
        writeConfigFile(configContent);

        // When: 설정 로드
        ReplayConfig config = ConfigLoader.loadConfig(configFile.toString());

        // Then: 기본값이 적용되었는지 확인
        assertEquals("localhost:9092", config.sourceBootstrapServers());
        assertEquals("localhost:9093", config.targetBootstrapServers());
        assertEquals("source-topic", config.sourceTopic());
        assertEquals("target-topic", config.targetTopic());
        assertEquals("replay-consumer-group", config.groupId()); // 기본값
        assertNull(config.partition());
        assertNull(config.offset());
        assertNull(config.timestamp());
        assertNull(config.maxMessages());
        assertEquals(0L, config.processingDelay()); // 기본값
        assertEquals(10, config.consumerTimeout()); // 기본값
    }

    @Test
    void testLoadConfig_withMissingSourceBootstrapServers_shouldThrowException() throws IOException {
        // Given: source bootstrap servers가 없는 설정 파일
        String configContent = """
            kafka.target.bootstrap.servers=localhost:9093
            kafka.source.topic=source-topic
            kafka.target.topic=target-topic
            """;
        
        writeConfigFile(configContent);

        // When & Then: 예외가 발생해야 함
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> ConfigLoader.loadConfig(configFile.toString())
        );
        
        assertEquals("kafka.source.bootstrap.servers is required", exception.getMessage());
    }

    @Test
    void testLoadConfig_withMissingTargetBootstrapServers_shouldThrowException() throws IOException {
        // Given: target bootstrap servers가 없는 설정 파일
        String configContent = """
            kafka.source.bootstrap.servers=localhost:9092
            kafka.source.topic=source-topic
            kafka.target.topic=target-topic
            """;
        
        writeConfigFile(configContent);

        // When & Then: 예외가 발생해야 함
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> ConfigLoader.loadConfig(configFile.toString())
        );
        
        assertEquals("kafka.target.bootstrap.servers is required", exception.getMessage());
    }

    @Test
    void testLoadConfig_withMissingSourceTopic_shouldThrowException() throws IOException {
        // Given: source topic이 없는 설정 파일
        String configContent = """
            kafka.source.bootstrap.servers=localhost:9092
            kafka.target.bootstrap.servers=localhost:9093
            kafka.target.topic=target-topic
            """;
        
        writeConfigFile(configContent);

        // When & Then: 예외가 발생해야 함
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> ConfigLoader.loadConfig(configFile.toString())
        );
        
        assertEquals("kafka.source.topic is required", exception.getMessage());
    }

    @Test
    void testLoadConfig_withMissingTargetTopic_shouldThrowException() throws IOException {
        // Given: target topic이 없는 설정 파일
        String configContent = """
            kafka.source.bootstrap.servers=localhost:9092
            kafka.target.bootstrap.servers=localhost:9093
            kafka.source.topic=source-topic
            """;
        
        writeConfigFile(configContent);

        // When & Then: 예외가 발생해야 함
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> ConfigLoader.loadConfig(configFile.toString())
        );
        
        assertEquals("kafka.target.topic is required", exception.getMessage());
    }

    @Test
    void testLoadConfig_withNonExistentFile_shouldThrowIOException() {
        // Given: 존재하지 않는 파일 경로
        String nonExistentPath = tempDir.resolve("non-existent.properties").toString();

        // When & Then: IOException이 발생해야 함
        assertThrows(
            IOException.class,
            () -> ConfigLoader.loadConfig(nonExistentPath)
        );
    }

    private void writeConfigFile(String content) throws IOException {
        try (FileWriter writer = new FileWriter(configFile.toFile())) {
            writer.write(content);
        }
    }
}
