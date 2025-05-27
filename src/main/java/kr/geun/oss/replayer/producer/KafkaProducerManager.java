package kr.geun.oss.replayer.producer;

import kr.geun.oss.replayer.config.ReplayConfig;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;

import java.util.Properties;

/**
 * KafkaProducerManager
 *
 * @author akageun
 * @since 2025-05-27
 */
public class KafkaProducerManager {


  public static KafkaProducer<String, String> createProducer(ReplayConfig config) {
    var props = buildProducerProperties(config);
    return new KafkaProducer<>(props);
  }

  private static Properties buildProducerProperties(ReplayConfig config) {
    var props = new Properties();

    props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, config.targetBootstrapServers());
    props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
    props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
    props.put(ProducerConfig.ACKS_CONFIG, "all");

    return props;
  }
}
