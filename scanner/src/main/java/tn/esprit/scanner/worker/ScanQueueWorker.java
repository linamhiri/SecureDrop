package tn.esprit.scanner.worker;

import com.azure.storage.queue.QueueClient;
import com.azure.storage.queue.models.QueueMessageItem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import tn.esprit.scanner.dto.ScanRequest;
import tn.esprit.scanner.service.BlobDispositionService;
import tn.esprit.scanner.service.BlobDownloadService;
import tn.esprit.scanner.service.ClamAvService;
import tn.esprit.scanner.service.ScanMetadataService;
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
    private final ClamAvService clamAvService;
    private final ScanMetadataService scanMetadataService;
    private final BlobDispositionService blobDispositionService;

    public ScanQueueWorker(
            QueueClient queueClient,
            JsonMapper jsonMapper,
            BlobDownloadService blobDownloadService,
            ClamAvService clamAvService,
            ScanMetadataService scanMetadataService,
            BlobDispositionService blobDispositionService) {

        this.queueClient = queueClient;
        this.jsonMapper = jsonMapper;
        this.blobDownloadService = blobDownloadService;
        this.clamAvService = clamAvService;
        this.scanMetadataService = scanMetadataService;
        this.blobDispositionService = blobDispositionService;
    }

    @Scheduled(fixedDelayString = "${scanner.poll-delay}")
    public void pollQueue() {

        for (QueueMessageItem message : queueClient.receiveMessages(1)) {

            Path downloadedFile = null;
            boolean processingSucceeded = false;

            try {

                // =====================================================
                // 1. READ QUEUE MESSAGE
                // =====================================================

                String body =
                        message.getBody().toString();


                // =====================================================
                // 2. PARSE SCAN REQUEST
                // =====================================================

                ScanRequest request =
                        jsonMapper.readValue(
                                body,
                                ScanRequest.class
                        );

                log.info("======================================");
                log.info("SCAN REQUEST RECEIVED");
                log.info("File ID: {}", request.getFileId());
                log.info("Blob name: {}", request.getBlobName());
                log.info("Container: {}", request.getContainerName());
                log.info(
                        "Dequeue count: {}",
                        message.getDequeueCount()
                );


                // =====================================================
                // 3. DOWNLOAD QUARANTINED BLOB
                // =====================================================

                downloadedFile =
                        blobDownloadService.downloadToTempFile(
                                request.getContainerName(),
                                request.getBlobName()
                        );

                log.info("Blob downloaded successfully");
                log.info("Temporary file: {}", downloadedFile);
                log.info(
                        "Size: {} bytes",
                        Files.size(downloadedFile)
                );


                // =====================================================
                // 4. SCAN WITH CLAMAV
                // =====================================================

                ClamAvService.ScanResult scanResult =
                        clamAvService.scan(downloadedFile);

                log.info(
                        "ClamAV response: {}",
                        scanResult.rawResponse()
                );


                // =====================================================
                // 5. PROCESS SCAN RESULT
                // =====================================================

                switch (scanResult.status()) {

                    // -------------------------------------------------
                    // CLEAN
                    // -------------------------------------------------

                    case CLEAN -> {

                        log.info("SCAN RESULT: CLEAN");

                        /*
                         * First update metadata.
                         *
                         * If this operation fails, the original blob is
                         * still in quarantine and the queue message can
                         * safely be retried.
                         */
                        scanMetadataService.updateStatus(
                                request.getFileId(),
                                "CLEAN"
                        );

                        log.info(
                                "Metadata status updated to CLEAN"
                        );

                        /*
                         * Move file:
                         *
                         * quarantine -> clean
                         */
                        blobDispositionService.moveToClean(
                                request.getContainerName(),
                                request.getBlobName(),
                                downloadedFile
                        );

                        log.info(
                                "Blob moved from quarantine to clean"
                        );

                        processingSucceeded = true;
                    }


                    // -------------------------------------------------
                    // INFECTED
                    // -------------------------------------------------

                    case INFECTED -> {

                        log.warn(
                                "SCAN RESULT: INFECTED - {}",
                                scanResult.virusName()
                        );

                        /*
                         * Record detection result.
                         */
                        scanMetadataService.updateStatus(
                                request.getFileId(),
                                "INFECTED"
                        );

                        log.info(
                                "Metadata status updated to INFECTED"
                        );

                        /*
                         * Move file:
                         *
                         * quarantine -> infected
                         */
                        blobDispositionService.moveToInfected(
                                request.getContainerName(),
                                request.getBlobName(),
                                downloadedFile
                        );

                        log.warn(
                                "Blob moved from quarantine to infected"
                        );

                        processingSucceeded = true;
                    }


                    // -------------------------------------------------
                    // ERROR
                    // -------------------------------------------------

                    case ERROR -> {

                        scanMetadataService.updateStatus(
                                request.getFileId(),
                                "ERROR"
                        );

                        log.error(
                                "SCAN RESULT: ERROR - {}",
                                scanResult.rawResponse()
                        );

                        /*
                         * Important:
                         *
                         * The blob stays in quarantine.
                         *
                         * The queue message is NOT deleted.
                         *
                         * Azure Queue can therefore retry the
                         * processing later.
                         */
                    }
                }


                // =====================================================
                // 6. DELETE QUEUE MESSAGE ONLY AFTER SUCCESS
                // =====================================================

                if (processingSucceeded) {

                    queueClient.deleteMessage(
                            message.getMessageId(),
                            message.getPopReceipt()
                    );

                    log.info(
                            "Queue message deleted successfully"
                    );
                }

                log.info("======================================");

            } catch (Exception e) {

                /*
                 * Any failure before queue deletion means that
                 * the message remains in Azure Queue.
                 *
                 * After the visibility timeout expires,
                 * the scanner can receive it again.
                 */
                log.error(
                        "Failed to process scan request message {}",
                        message.getMessageId(),
                        e
                );

            } finally {

                // =====================================================
                // 7. ALWAYS REMOVE LOCAL TEMPORARY FILE
                // =====================================================

                if (downloadedFile != null) {

                    try {

                        Files.deleteIfExists(downloadedFile);

                        log.info(
                                "Temporary file deleted"
                        );

                    } catch (Exception e) {

                        log.warn(
                                "Could not delete temporary file: {}",
                                downloadedFile,
                                e
                        );
                    }
                }
            }
        }
    }
}