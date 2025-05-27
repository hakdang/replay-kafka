# Kafka Replayer

Apache Kafka 토픽 간에 메시지를 재생(replay)하는 도구입니다. 소스 토픽의 메시지를 읽어서 타겟 토픽으로 복사하는 기능을 제공하며, 다양한 재생 옵션을 지원합니다.

## 🌟 주요 기능

- **토픽 간 메시지 복사**: 소스 토픽의 메시지를 타겟 토픽으로 재생
- **다양한 Consumer 모드**: Subscribe 모드와 Assign 모드 지원
- **Offset 기반 재생**: 특정 오프셋부터 메시지 재생
- **Timestamp 기반 재생**: 특정 시간 이후의 메시지 재생
- **메시지 수 제한**: 최대 재생할 메시지 수 제한
- **처리 지연 설정**: 메시지 처리 간 지연 시간 설정
- **자동 타임아웃**: 설정된 시간 동안 메시지가 없으면 자동 종료
- **클러스터 간 복제**: 서로 다른 Kafka 클러스터 간 메시지 복제 지원

## 📦 설치 및 빌드

### 요구사항
- Java 17 이상
- Apache Kafka 3.7.0
- Gradle 7.x 이상

### 빌드
```bash
./gradlew build
```

### JAR 생성
```bash
./gradlew jar
```

## ⚙️ 설정

### 설정 파일 형식
`application.properties` 파일에 다음과 같이 설정합니다:

```properties
# 필수 설정
kafka.source.bootstrap.servers=localhost:9092
kafka.target.bootstrap.servers=localhost:9092
kafka.source.topic=source-topic
kafka.target.topic=target-topic

# Consumer 설정
kafka.group.id=replay-consumer-group

# 선택적 설정
kafka.partition=0                    # 특정 파티션 지정 (생략시 subscribe 모드)
kafka.offset=100                     # 시작 오프셋 (partition과 함께 사용)
kafka.timestamp=2024-01-01T00:00:00Z # 시작 타임스탬프 (ISO-8601 형식)
kafka.max.messages=1000              # 최대 재생 메시지 수
kafka.processing.delay=100           # 메시지 처리 간 지연 시간 (ms)
kafka.consumer.timeout=10            # Consumer 타임아웃 (초)
```

### 설정 옵션 상세

| 설정 키 | 필수 | 기본값 | 설명 |
|---------|------|-------|------|
| `kafka.source.bootstrap.servers` | ✅ | - | 소스 Kafka 클러스터 주소 |
| `kafka.target.bootstrap.servers` | ✅ | - | 타겟 Kafka 클러스터 주소 |
| `kafka.source.topic` | ✅ | - | 소스 토픽명 |
| `kafka.target.topic` | ✅ | - | 타겟 토픽명 |
| `kafka.group.id` | ⚪ | `replay-consumer-group` | Consumer 그룹 ID |
| `kafka.partition` | ⚪ | - | 특정 파티션 번호 (생략시 전체 토픽 구독) |
| `kafka.offset` | ⚪ | - | 시작 오프셋 (`partition`과 함께 사용) |
| `kafka.timestamp` | ⚪ | - | 시작 타임스탬프 (ISO-8601 형식) |
| `kafka.max.messages` | ⚪ | - | 최대 재생 메시지 수 |
| `kafka.processing.delay` | ⚪ | `0` | 메시지 처리 간 지연 시간 (밀리초) |
| `kafka.consumer.timeout` | ⚪ | `10` | Consumer 타임아웃 (초) |

## 🚀 사용법

### 1. 기본 실행
```bash
java -jar kafka-replayer.jar config.properties
```

### 2. 사용 예시

#### 전체 토픽 재생 (Subscribe 모드)
```properties
kafka.source.bootstrap.servers=localhost:9092
kafka.target.bootstrap.servers=localhost:9092
kafka.source.topic=orders
kafka.target.topic=orders-copy
kafka.group.id=replay-group
```

#### 특정 파티션의 특정 오프셋부터 재생
```properties
kafka.source.bootstrap.servers=localhost:9092
kafka.target.bootstrap.servers=localhost:9092
kafka.source.topic=events
kafka.target.topic=events-backup
kafka.partition=0
kafka.offset=1000
```

#### 특정 시간 이후의 메시지 재생
```properties
kafka.source.bootstrap.servers=localhost:9092
kafka.target.bootstrap.servers=localhost:9092
kafka.source.topic=logs
kafka.target.topic=logs-archive
kafka.timestamp=2024-01-01T00:00:00Z
kafka.max.messages=5000
```

#### 처리 지연을 둔 재생
```properties
kafka.source.bootstrap.servers=localhost:9092
kafka.target.bootstrap.servers=localhost:9092
kafka.source.topic=transactions
kafka.target.topic=transactions-replay
kafka.processing.delay=500
kafka.max.messages=100
```

## 🏗️ 아키텍처

### 주요 컴포넌트

1. **KafkaRePlayer**: 메인 애플리케이션 클래스
2. **ConfigLoader**: 설정 파일 로딩 및 검증
3. **ReplayConfig**: 재생 설정을 담는 설정 객체 (Record 타입)
4. **KafkaConsumerManager**: Kafka Consumer 생성 및 설정
5. **KafkaProducerManager**: Kafka Producer 생성 및 설정
6. **MessageReplayService**: 메시지 재생 핵심 로직

### 동작 플로우

```
1. 설정 파일 로드 (ConfigLoader)
2. Kafka Consumer/Producer 생성 (Manager 클래스들)
3. Consumer 모드 설정 (Subscribe/Assign)
4. 메시지 폴링 및 재생 (MessageReplayService)
5. 통계 정보 출력 및 종료
```

### Consumer 모드

#### Subscribe 모드
- `kafka.partition`이 설정되지 않은 경우
- 전체 토픽을 구독하여 메시지 수신
- Consumer Group을 사용한 로드 밸런싱

#### Assign 모드  
- `kafka.partition`이 설정된 경우
- 특정 파티션만 할당하여 메시지 수신
- 오프셋 또는 타임스탬프 기반 시작 위치 설정 가능

```properties
kafka.partition=0
# kafka.group.id 설정하지 않음 (선택적)
```

## 📊 통계 정보

재생 완료 후 다음 통계 정보가 출력됩니다:

- 처리된 메시지 수
- 총 소요 시간
- 초당 처리량 (TPS)

## 📁 프로젝트 구조

```
src/
├── main/java/kr/geun/oss/replayer/
│   ├── KafkaRePlayer.java              # 메인 애플리케이션
│   ├── MessageReplayService.java       # 메시지 재생 로직
│   ├── config/
│   │   ├── ConfigLoader.java          # 설정 로더
│   │   └── ReplayConfig.java          # 설정 데이터 클래스
│   ├── consumer/
│   │   └── KafkaConsumerManager.java  # Kafka Consumer 관리
│   └── producer/
│       └── KafkaProducerManager.java  # Kafka Producer 관리
└── test/                              # 단위 테스트
```

## 🧪 테스트

### 전체 테스트 실행

```bash
./gradlew test
```

### 테스트 커버리지 확인

```bash
./gradlew test jacocoTestReport
open build/reports/jacoco/test/html/index.html
```

### 테스트 구성

- **단위 테스트**: 각 컴포넌트의 개별 기능 테스트
- **설정 테스트**: ConfigLoader, ReplayConfig 검증
- **Manager 테스트**: Consumer/Producer 관리자 기능 테스트
- **서비스 테스트**: MessageReplayService 로직 테스트 (모킹 사용)

## 📝 사용 예시

### 1. 기본 재생
특정 토픽의 모든 메시지를 다른 토픽으로 복사:

```properties
kafka.source.bootstrap.servers=localhost:9092
kafka.target.bootstrap.servers=localhost:9092
kafka.source.topic=orders
kafka.target.topic=orders-backup
kafka.group.id=backup-group
```

### 2. 특정 오프셋부터 재생
오프셋 100부터 1000개의 메시지만 재생:

```properties
kafka.source.bootstrap.servers=localhost:9092
kafka.target.bootstrap.servers=localhost:9092
kafka.source.topic=events
kafka.target.topic=events-replay
kafka.partition=0
kafka.offset=100
kafka.max.messages=1000
```

### 3. 특정 시간 이후 메시지 재생
2024년 1월 1일 이후의 메시지를 재생:

```properties
kafka.source.bootstrap.servers=localhost:9092
kafka.target.bootstrap.servers=localhost:9092
kafka.source.topic=logs
kafka.target.topic=logs-filtered
kafka.group.id=filter-group
kafka.timestamp=2024-01-01T00:00:00Z
```

### 4. 클러스터 간 복제
서로 다른 Kafka 클러스터 간 메시지 복제:

```properties
kafka.source.bootstrap.servers=source-kafka:9092
kafka.target.bootstrap.servers=target-kafka:9092
kafka.source.topic=production-data
kafka.target.topic=staging-data
kafka.group.id=cross-cluster-group
kafka.processing.delay=50
```

## 🛠️ 개발 가이드

### 빌드 시스템
- **Gradle**: 빌드 도구
- **Dependencies**: Kafka Client 3.7.0, SLF4J

### 코드 스타일
- Java 17+ Record 패턴 사용
- 함수형 프로그래밍 스타일 선호
- 상세한 로깅과 에러 처리

### 테스트 전략
- 단위 테스트: Mockito를 사용한 격리된 테스트
- 통합 테스트 제외: 실제 Kafka 의존성 없는 테스트
- 설정 검증: 다양한 설정 시나리오 테스트

## 🚨 주의사항

1. **메시지 순서**: 파티션 내에서는 순서가 보장되지만, 파티션 간에는 보장되지 않습니다.
2. **중복 처리**: 재시작 시 중복 메시지가 발생할 수 있습니다.
3. **메모리 사용량**: 대용량 메시지 처리 시 메모리 사용량을 모니터링하세요.
4. **네트워크**: 클러스터 간 복제 시 네트워크 지연을 고려하세요.

## 🐛 문제 해결

### 연결 오류
```
org.apache.kafka.common.config.ConfigException: No resolvable bootstrap urls
```
- bootstrap.servers 설정을 확인하세요
- Kafka 브로커가 실행 중인지 확인하세요

### 토픽 없음 오류
```
org.apache.kafka.common.errors.UnknownTopicOrPartitionException
```
- 소스/타겟 토픽이 존재하는지 확인하세요
- auto.create.topics.enable 설정을 확인하세요

### 권한 오류
```
org.apache.kafka.common.errors.TopicAuthorizationException
```
- Consumer/Producer 권한을 확인하세요
- ACL 설정을 확인하세요
