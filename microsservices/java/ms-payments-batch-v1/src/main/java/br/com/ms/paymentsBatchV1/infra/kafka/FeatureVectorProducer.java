package br.com.ms.paymentsBatchV1.infra.kafka;

import br.com.ms.paymentsBatchV1.model.*;
import lombok.RequiredArgsConstructor;

import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class FeatureVectorProducer {

  private final KafkaTemplate<String, FeatureVector> kafkaTemplate;

  @Value("${app.kafka.topic}")
  private String topic;

  public void send(FeatureVector fv) {
    kafkaTemplate.send(topic, UUID.randomUUID().toString(), fv);
  }
}
