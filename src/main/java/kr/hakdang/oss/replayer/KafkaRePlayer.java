package kr.hakdang.oss.replayer;

import kr.hakdang.oss.replayer.config.ConfigLoader;
import kr.hakdang.oss.replayer.config.ReplayConfig;
import kr.hakdang.oss.replayer.consumer.KafkaConsumerManager;
import kr.hakdang.oss.replayer.producer.KafkaProducerManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

/**
 * KafkaRePlayer
 *
 * @author akageun
 * @since 2025-05-27
 */
public class KafkaRePlayer {

  private static final Logger logger = LoggerFactory.getLogger(KafkaRePlayer.class);

  public static void main(String[] args) {
    try {
      var replayApp = new KafkaRePlayer();
      replayApp.run();

    } catch (Exception e) {
      logger.error("Application failed to start", e);
      System.exit(1);
    }
  }

  public void run() throws IOException {
    ReplayConfig config = ConfigLoader.loadConfig();

    // 2. Kafka 클라이언트 생성
    var consumer = KafkaConsumerManager.createConsumer(config);
    var producer = KafkaProducerManager.createProducer(config);

    try (consumer; producer) {
      // 3. Consumer 설정
      KafkaConsumerManager.setupConsumer(consumer, config);

      // 4. 메시지 리플레이 실행
      var replayService = new MessageReplayService(consumer, producer, config);
      replayService.startReplay();

    } catch (Throwable e) {
      logger.error("Error during replay process", e);
    } finally {
      logger.info("Replay system closed");
    }
  }
}
