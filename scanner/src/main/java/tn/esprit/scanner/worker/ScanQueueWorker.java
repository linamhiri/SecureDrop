package tn.esprit.scanner.worker;

import com.azure.storage.queue.QueueClient;
import com.azure.storage.queue.models.QueueMessageItem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import tn.esprit.scanner.dto.ScanRequest;
import tn.esprit.scanner.service.BlobDownloadService;
import tools.jackson.databind.json.JsonMapper;

import java.nio.file.Files;
import java.nio.file.Path;

@Component
public class ScanQueueWorker {

    private static final Logger log =
            LoggerFactory.getLogger(ScanQueueWorker.class);

    private final QueueClient queueClient;
    private final JsonMapper jsonMapper;
    private final BlobDownloadService blobDownloadService;

    public ScanQueueWorker(
            QueueClient queueClient,
            JsonMapper jsonMapper,
            BlobDownloadService blobDownloadService) {

        this.queueClient = queueClient;
        this.jsonMapper = jsonMapper;
        this.blobDownloadService = blobDownloadService;
    }

    @Scheduled(fixedDelayString = "${scanner.poll-delay}")
    public void pollQueue() {

        for (QueueMessageItem message : queueClient.receiveMessages(1)) {

            try {

                // 1. Read the JSON message from Azure Queue
                String body = message.getBody().toString();

                // 2. Convert JSON into ScanRequest
                ScanRequest request =
                        jsonMapper.readValue(body, ScanRequest.class);

                log.info("======================================");
                log.info("SCAN REQUEST PARSED");
                log.info("File ID: {}", request.getFileId());
                log.info("Blob name: {}", request.getBlobName());
                log.info("Container: {}", request.getContainerName());
                log.info("Requested at: {}", request.getRequestedAt());
                log.info("Dequeue count: {}", message.getDequeueCount());

                // 3. Download the real file from Blob Storage
                Path downloadedFile =
                        blobDownloadService.downloadToTempFile(
                                request.getContainerName(),
                                request.getBlobName()
                        );

                // 4. Confirm the file was downloaded
                log.info("Blob downloaded successfully");
                log.info("Temporary file: {}", downloadedFile);
                log.info("Size: {} bytes", Files.size(downloadedFile));

                log.info("======================================");

                /*
                 * IMPORTANT:
                 * We deliberately do NOT delete the queue message yet.
                 *
                 * Later:
                 *
                 * download
                 *    ↓
                 * ClamAV scan
                 *    ↓
                 * update status
                 *    ↓
                 * delete queue message
                 */

            } catch (Exception e) {

                log.error(
                        "Failed to process scan request message {}",
                        message.getMessageId(),
                        e
                );
            }
        }
    }
}