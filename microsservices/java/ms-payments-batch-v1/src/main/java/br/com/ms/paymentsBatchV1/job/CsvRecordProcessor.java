package br.com.ms.paymentsBatchV1.job;

import br.com.ms.paymentsBatchV1.infra.csv.FilesystemCsvReader;
import br.com.ms.paymentsBatchV1.infra.kafka.FeatureVectorProducer;
import br.com.ms.paymentsBatchV1.model.FeatureVector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Processes CSV records and converts them to FeatureVector objects.
 */
@Component
public class CsvRecordProcessor {

  private static final Logger logger = LoggerFactory.getLogger(CsvRecordProcessor.class);

  @Autowired
  private FeatureVectorProducer featureVectorProducer;

  /**
   * Process CSV records and send FeatureVectors to Kafka.
   *
   * @param records the CSV records read from file
   */
  public void processAndSendToKafka(List<FilesystemCsvReader.CSVRecordWrapper> records) {
    if (records == null || records.isEmpty()) {
      logger.warn("No records to process");
      return;
    }

    logger.info("Processing {} records", records.size());

    for (FilesystemCsvReader.CSVRecordWrapper record : records) {
      try {
        FeatureVector vector = parseRecord(record);
        if (vector != null) {
          featureVectorProducer.send(vector);
          logger.debug("Sent FeatureVector: A_ID={}, B_ID={}", vector.getAId(), vector.getBId());
        }
      } catch (Exception e) {
        logger.error("Error processing record at line {}: {}", 
            record.lineNumber(), e.getMessage(), e);
      }
    }

    logger.info("Finished processing {} records", records.size());
  }

  /**
   * Parse a CSV record into a FeatureVector object.
   * Assumes CSV format: aId, bId, amtRatio, ddRatio, hitCount, rare2, ss, tfidfCos
   *
   * @param record the CSV record wrapper
   * @return FeatureVector or null if parsing fails
   */
  private FeatureVector parseRecord(FilesystemCsvReader.CSVRecordWrapper record) {
    try {
      String[] parts = record.rawLine().split(",");
      
      if (parts.length < 8) {
        logger.warn("Invalid record format at line {}: expected 8 fields, got {}", 
            record.lineNumber(), parts.length);
        return null;
      }

      return FeatureVector.builder()
          .aId(parts[0].trim())
          .bId(parts[1].trim())
          .amtRatio(Double.parseDouble(parts[2].trim()))
          .ddRatio(Double.parseDouble(parts[3].trim()))
          .hitCount(Integer.parseInt(parts[4].trim()))
          .rare2(Integer.parseInt(parts[5].trim()))
          .ss(Double.parseDouble(parts[6].trim()))
          .tfidfCos(Double.parseDouble(parts[7].trim()))
          .build();

    } catch (NumberFormatException e) {
      logger.error("Number format error parsing record at line {}: {}", 
          record.lineNumber(), e.getMessage());
      return null;
    } catch (Exception e) {
      logger.error("Unexpected error parsing record at line {}: {}", 
          record.lineNumber(), e.getMessage(), e);
      return null;
    }
  }
}
