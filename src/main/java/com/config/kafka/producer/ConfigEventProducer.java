package com.config.kafka.producer;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class ConfigEventProducer {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    private static final String TOPIC_TAXONOMY_CHANGED = "config.taxonomy.changed";

    /**
     * 지역·카테고리·서브카테고리·코드 변경 시 발행
     * 전 서비스에서 소비 → 로컬 코드 캐시 무효화 및 갱신
     */
    public void publishTaxonomyChanged(String changeType, String changedEntity) {
        Map<String, Object> payload = Map.of(
                "changeType", changeType,     // REGION_ADDED, CATEGORY_ADDED, CODE_UPDATED 등
                "changedEntity", changedEntity
        );
        kafkaTemplate.send(TOPIC_TAXONOMY_CHANGED, changeType, payload);
        log.info("[Kafka] config.taxonomy.changed published: changeType={}, entity={}",
                changeType, changedEntity);
    }
}
