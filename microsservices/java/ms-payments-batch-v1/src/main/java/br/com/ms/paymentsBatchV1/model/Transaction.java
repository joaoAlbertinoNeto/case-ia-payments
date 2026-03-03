package br.com.ms.paymentsBatchV1.model;

import lombok.Builder;
import lombok.Value;

import java.math.BigDecimal;
import java.time.LocalDate;

@Value
@Builder
public class Transaction {
  TransactionSide side;
  String id;
  String transactionType;
  String debitOrCredit; // CR / DR
  BigDecimal amount;
  LocalDate valueDate;
  String currencyCode;
  String account;

  String transactionReferences;
  String transactionAttributes;

  public String text() {
    String refs = transactionReferences == null ? "" : transactionReferences.trim();
    String attrs = transactionAttributes == null ? "" : transactionAttributes.trim();
    String combined = (refs + " " + attrs).trim();
    return combined;
  }
}
