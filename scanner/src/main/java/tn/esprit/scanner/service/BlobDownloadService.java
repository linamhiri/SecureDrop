package tn.esprit.scanner.service;

import com.azure.storage.blob.BlobClient;
import com.azure.storage.blob.BlobServiceClient;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;

@Service
public class BlobDownloadService {

    private final BlobServiceClient blobServiceClient;

    public BlobDownloadService(BlobServiceClient blobServiceClient) {
        this.blobServiceClient = blobServiceClient;
    }

    public Path downloadToTempFile(String containerName, String blobName)
            throws IOException {

        BlobClient blobClient = blobServiceClient
                .getBlobContainerClient(containerName)
                .getBlobClient(blobName);

        if (!blobClient.exists()) {
            throw new IllegalStateException(
                    "Blob not found: " + containerName + "/" + blobName
            );
        }

        Path tempFile =
                Files.createTempFile("securedrop-scan-", ".tmp");

        try (OutputStream outputStream = Files.newOutputStream(tempFile)) {

            blobClient.downloadStream(outputStream);

        } catch (Exception e) {

            Files.deleteIfExists(tempFile);
            throw e;
        }

        return tempFile;
    }
}