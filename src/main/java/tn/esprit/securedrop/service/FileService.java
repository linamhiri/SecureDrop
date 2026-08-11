package tn.esprit.securedrop.service;

import tn.esprit.securedrop.config.StorageProperties;
import tn.esprit.securedrop.dto.ScanRequest;
import tn.esprit.securedrop.model.FileMetadata;
import tn.esprit.securedrop.model.FileStatus;
import tn.esprit.securedrop.repository.FileMetadataRepository;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.time.Instant;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;

@Service
public class FileService {

    private static final long MAX_FILE_SIZE =
            25L * 1024 * 1024;

    private final BlobStorageService blobStorageService;
    private final ScanQueueService scanQueueService;
    private final FileMetadataRepository fileMetadataRepository;
    private final StorageProperties storageProperties;

    public FileService(
            BlobStorageService blobStorageService,
            ScanQueueService scanQueueService,
            FileMetadataRepository fileMetadataRepository,
            StorageProperties storageProperties) {

        this.blobStorageService = blobStorageService;
        this.scanQueueService = scanQueueService;
        this.fileMetadataRepository = fileMetadataRepository;
        this.storageProperties = storageProperties;
    }

    public FileMetadata upload(MultipartFile file) {

        validateFile(file);

        String fileId = UUID.randomUUID().toString();

        String originalFilename =
                sanitizeFilename(file.getOriginalFilename());

        String blobName =
                fileId + "-" + originalFilename;

        /*
         * Step 1:
         * Upload the physical file to Azure Blob Storage.
         */
        blobStorageService.upload(file, blobName);

        /*
         * Step 2:
         * Save portal metadata.
         */
        FileMetadata metadata =
                new FileMetadata(
                        fileId,
                        originalFilename,
                        blobName,
                        file.getContentType(),
                        file.getSize(),
                        FileStatus.UPLOADED,
                        Instant.now()
                );

        fileMetadataRepository.save(metadata);

        /*
         * Step 3:
         * Send scan request.
         */
        ScanRequest scanRequest =
                new ScanRequest(
                        fileId,
                        blobName,
                        storageProperties.getContainerName(),
                        Instant.now()
                );

        try {

            scanQueueService.enqueue(scanRequest);

            metadata.setStatus(
                    FileStatus.PENDING_SCAN
            );

            fileMetadataRepository.save(metadata);

        } catch (RuntimeException exception) {

            metadata.setStatus(
                    FileStatus.ERROR
            );

            fileMetadataRepository.save(metadata);

            throw new IllegalStateException(
                    "The file was uploaded, but the scan request could not be queued.",
                    exception
            );
        }

        return metadata;
    }

    public List<FileMetadata> findAll() {
        return fileMetadataRepository.findAll();
    }

    public FileMetadata findById(String id) {

        return fileMetadataRepository
                .findById(id)
                .orElseThrow(
                        () -> new NoSuchElementException(
                                "File not found."
                        )
                );
    }

    public FileMetadata updateStatus(
            String id,
            FileStatus status) {

        FileMetadata metadata =
                findById(id);

        metadata.setStatus(status);

        return fileMetadataRepository.save(metadata);
    }

    private void validateFile(MultipartFile file) {

        if (file == null || file.isEmpty()) {

            throw new IllegalArgumentException(
                    "Please select a file."
            );
        }

        if (file.getSize() > MAX_FILE_SIZE) {

            throw new IllegalArgumentException(
                    "The file exceeds the maximum size of 25 MB."
            );
        }

        if (file.getOriginalFilename() == null
                || file.getOriginalFilename().isBlank()) {

            throw new IllegalArgumentException(
                    "Invalid file name."
            );
        }
    }

    private String sanitizeFilename(
            String originalFilename) {

        String filename =
                StringUtils.cleanPath(originalFilename);

        if (filename.contains("..")) {

            throw new IllegalArgumentException(
                    "Invalid file name."
            );
        }

        filename =
                filename.replaceAll(
                        "[^a-zA-Z0-9._-]",
                        "_"
                );

        if (filename.isBlank()) {

            throw new IllegalArgumentException(
                    "Invalid file name."
            );
        }

        return filename;
    }
}