package br.com.ms.paymentsBatchV1.job;

import br.com.ms.paymentsBatchV1.infra.csv.FilesystemCsvReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Stream;

@Component
@EnableScheduling
public class CsvFileReadJob {

  private static final Logger logger = LoggerFactory.getLogger(CsvFileReadJob.class);

  @Autowired
  private FilesystemCsvReader csvReader;

  @Autowired
  private CsvRecordProcessor csvRecordProcessor;

  @Value("${csv.input.directory:./data}")
  private String inputDirectory;

  /**
   * Executes every 5 seconds (5000 milliseconds).
   * This job reads all CSV files from the configured input directory
   * and processes them.
   */
  @Scheduled(fixedDelay = 5000)
  public void readCsvFilesWithDelay() {
    try {
      logger.info("Starting CSV file read job at {}", System.currentTimeMillis());
      
      Path dirPath = Paths.get(inputDirectory);
      
      if (!Files.exists(dirPath)) {
        logger.warn("Input directory does not exist: {}", inputDirectory);
        return;
      }

      // Find all CSV files in the directory
      try (Stream<Path> paths = Files.list(dirPath)) {
        List<Path> csvFiles = paths
            .filter(p -> p.toString().endsWith(".csv"))
            .toList();

        if (csvFiles.isEmpty()) {
          logger.debug("No CSV files found in directory: {}", inputDirectory);
          return;
        }

        logger.info("Found {} CSV file(s) to process", csvFiles.size());

        for (Path csvFile : csvFiles) {
          try {
            logger.debug("Reading file: {}", csvFile.getFileName());
            
            var records = csvReader.read(csvFile);
            
            logger.info("Successfully read {} records from file: {}", 
                records.size(), csvFile.getFileName());
            
            // Process records
            csvRecordProcessor.processAndSendToKafka(records);
            
          } catch (Exception e) {
            logger.error("Error reading CSV file: {}", csvFile, e);
          }
        }
      }
      
      logger.info("CSV file read job completed");
      
    } catch (Exception e) {
      logger.error("Unexpected error in CsvFileReadJob", e);
    }
  }
}
