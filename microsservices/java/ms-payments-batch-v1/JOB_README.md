# CSV File Read Job

A scheduled job that reads CSV files from a configured directory every 5 seconds and processes them into `FeatureVector` objects to send to Kafka.

## Classes

### CsvFileReadJob
Main scheduled job that:
- **Runs every 5 seconds** (configurable via `@Scheduled`)
- Scans the input directory for `.csv` files
- Reads each CSV file using `FilesystemCsvReader`
- Delegates processing to `CsvRecordProcessor`
- Logs all operations

**Configuration:**
```yaml
csv:
  input:
    directory: ./data  # Can override with CSV_INPUT_DIRECTORY env var
```

### CsvRecordProcessor
Handles the conversion of CSV raw records to domain objects:
- Parses CSV record strings into `FeatureVector` objects
- Validates field format (expects 8 fields)
- Sends `FeatureVector` objects to Kafka via `FeatureVectorProducer`
- Logs parsing errors with line numbers for debugging

**Expected CSV Format:**
```
aId, bId, amtRatio, ddRatio, hitCount, rare2, ss, tfidfCos
```

Example:
```
TRANS001, TRANS456, 0.95, 1.0, 4, 0, 2.5, 0.85
TRANS002, TRANS789, 0.05, 0.5, 8, 2, 3.2, 0.92
```

## Setup

1. **Enable Scheduling** – Done in `Application.java`:
   ```java
   @SpringBootApplication
   @EnableScheduling
   public class Application { }
   ```

2. **Configure Input Directory** – In `application.yml`:
   ```yaml
   csv:
     input:
       directory: ./data
   ```
   Or set environment variable:
   ```bash
   export CSV_INPUT_DIRECTORY=/path/to/csv/files
   ```

3. **Test with Sample CSV File**
   ```bash
   mkdir -p ./data
   echo "TRANS001,TRANS456,0.95,1.0,4,0,2.5,0.85" > ./data/test.csv
   ```

4. **Run the Application**
   ```bash
   mvn spring-boot:run
   ```

## Scheduling Options

Currently set to run every 5 seconds. To customize:

**Fixed Delay** (waits 5s between executions):
```java
@Scheduled(fixedDelay = 5000)
```

**Fixed Rate** (executes every 5s regardless of duration):
```java
@Scheduled(fixedRate = 5000)
```

**CRON Expression** (e.g., every hour at minute 30):
```java
@Scheduled(cron = "0 30 * * * *")
```

**Initial Delay** (waits 10s before first run):
```java
@Scheduled(fixedDelay = 5000, initialDelay = 10000)
```

## Logging

All operations are logged at appropriate levels:
- **INFO**: Job start/completion, file counts, record counts
- **DEBUG**: Individual file reads, record processing
- **WARN**: Missing directories, malformed records
- **ERROR**: Exceptions during read/parse

View logs:
```bash
mvn spring-boot:run | grep CsvFileReadJob
```

## Integration with Kafka

Processed `FeatureVector` objects are sent to the configured Kafka topic via `FeatureVectorProducer`:

```yaml
app:
  kafka:
    topic: payments-events  # Can override with FEATURE_TOPIC env var
```

## Error Handling

- Missing input directory → logs warning, skips job
- Missing CSV files → logs debug message, continues
- Parse errors → logs line number + error, continues processing remaining records
- Kafka send failures → handled by producer, logged appropriately

## Example Workflow

```
1. Job runs every 5 seconds
2. Scans ./data for *.csv files
3. Reads "transactions.csv" → 1000 records
4. CsvRecordProcessor parses each record
5. Validates 8 fields per record
6. Converts to FeatureVector objects
7. Sends to Kafka topic "payments-events"
8. Logs completion with count
9. Waits 5 seconds, repeats
```
