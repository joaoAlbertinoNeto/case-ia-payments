package br.com.ms.paymentsBatchV1.model;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class FeatureVector {
  String aId;
  String bId;
  double amtRatio;
  double ddRatio;
  int hitCount;
  int rare2;
  double ss;
  double tfidfCos;
}