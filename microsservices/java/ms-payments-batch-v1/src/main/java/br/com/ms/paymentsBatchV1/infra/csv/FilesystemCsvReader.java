package br.com.ms.paymentsBatchV1.infra.csv;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.springframework.stereotype.Component;

import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

@Component
public class FilesystemCsvReader {

  public List<CSVRecordWrapper> read(Path path) {
    try (Reader reader = Files.newBufferedReader(path);
         CSVParser parser = CSVFormat.DEFAULT
             .withFirstRecordAsHeader()
             .withIgnoreEmptyLines()
             .withTrim()
             .parse(reader)) {

      List<CSVRecordWrapper> out = new ArrayList<>();
      long line = 1;
      for (CSVRecord r : parser) {
        out.add(new CSVRecordWrapper(line++, r.toString()));
      }
      return out;

    } catch (Exception e) {
      throw new IllegalArgumentException("Erro lendo arquivo: " + path + " -> " + e.getMessage(), e);
    }
  }

  public record CSVRecordWrapper(long lineNumber, String rawLine) {}
}
