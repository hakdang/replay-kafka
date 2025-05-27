package kr.hakdang.oss.replayer.config;

import java.time.Instant;

/**
 * ReplayConfig
 *
 * @author akageun
 * @since 2025-05-27
 */
public record ReplayConfig(
  String sourceBootstrapServers,
  String targetBootstrapServers,
  String sourceTopic,
  String targetTopic,
  String groupId,
  Integer partition,
  Long offset,
  Instant timestamp,
  Integer maxMessages,
  long processingDelay,
  int consumerTimeout
) {

  public ReplayConfig {
    // constructor 로 유효성 검사
    if (sourceBootstrapServers == null || sourceBootstrapServers.isBlank()) {
      throw new IllegalArgumentException("kafka.source.bootstrap.servers is required");
    }
    if (targetBootstrapServers == null || targetBootstrapServers.isBlank()) {
      throw new IllegalArgumentException("kafka.target.bootstrap.servers is required");
    }
    if (sourceTopic == null || sourceTopic.isBlank()) {
      throw new IllegalArgumentException("kafka.source.topic is required");
    }
    if (targetTopic == null || targetTopic.isBlank()) {
      throw new IllegalArgumentException("kafka.target.topic is required");
    }
  }

  /**
   * Subscribe 모드인지 확인 (partition 이 지정되지 않은 경우)
   */
  public boolean isSubscribeMode() {
    return partition == null;
  }

  /**
   * 오프셋 기반 리플레이인지 확인
   */
  public boolean isOffsetBased() {
    return offset != null;
  }

  /**
   * 타임스탬프 기반 리플레이인지 확인
   */
  public boolean isTimestampBased() {
    return timestamp != null;
  }

  /**
   * 최대 메시지 제한이 있는지 확인
   */
  public boolean hasMaxMessageLimit() {
    return maxMessages != null && maxMessages > 0;
  }

  /**
   * 처리 지연이 설정되었는지 확인
   */
  public boolean hasProcessingDelay() {
    return processingDelay > 0;
  }

  @Override
  public String sourceBootstrapServers() {
    return sourceBootstrapServers;
  }

  @Override
  public String targetBootstrapServers() {
    return targetBootstrapServers;
  }

  @Override
  public String sourceTopic() {
    return sourceTopic;
  }

  @Override
  public String targetTopic() {
    return targetTopic;
  }

  @Override
  public String groupId() {
    return groupId;
  }

  @Override
  public Integer partition() {
    return partition;
  }

  @Override
  public Long offset() {
    return offset;
  }

  @Override
  public Instant timestamp() {
    return timestamp;
  }

  @Override
  public Integer maxMessages() {
    return maxMessages;
  }

  @Override
  public long processingDelay() {
    return processingDelay;
  }

  @Override
  public int consumerTimeout() {
    return consumerTimeout;
  }
}
