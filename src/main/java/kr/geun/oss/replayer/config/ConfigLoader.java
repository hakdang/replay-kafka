package kr.geun.oss.replayer.config;

import java.io.FileInputStream;
import java.io.IOException;
import java.time.Instant;
import java.util.Properties;

/**
 * 설정 파일을 로드하고 ReplayConfig 객체를 생성하는 팩토리 클래스
 */
public class ConfigLoader {
    private static final String DEFAULT_CONFIG_PATH = "src/main/resources/application.properties";

    public static ReplayConfig loadConfig() throws IOException {
        return loadConfig(DEFAULT_CONFIG_PATH);
    }

    public static ReplayConfig loadConfig(String configPath) throws IOException {
        var props = new Properties();
        try (var fis = new FileInputStream(configPath)) {
            props.load(fis);
        }
        return createConfig(props);
    }

    private static ReplayConfig createConfig(Properties props) {
        return new ReplayConfig(
            props.getProperty("kafka.source.bootstrap.servers"),
            props.getProperty("kafka.target.bootstrap.servers"),
            props.getProperty("kafka.source.topic"),
            props.getProperty("kafka.target.topic"),
            props.getProperty("kafka.group.id", "replay-consumer-group"),
            parseInteger(props.getProperty("kafka.partition")),
            parseLong(props.getProperty("kafka.offset")),
            parseInstant(props.getProperty("kafka.timestamp")),
            parseInteger(props.getProperty("kafka.max.messages")),
            parseLong(props.getProperty("kafka.processing.delay", "0")),
            parseInt(props.getProperty("kafka.consumer.timeout", "10"))
        );
    }

    private static Integer parseInteger(String value) {
        return value != null ? Integer.parseInt(value) : null;
    }

    private static Long parseLong(String value) {
        return value != null ? Long.parseLong(value) : null;
    }

    private static int parseInt(String value) {
        return Integer.parseInt(value);
    }

    private static Instant parseInstant(String value) {
        return value != null ? Instant.parse(value) : null;
    }
}
