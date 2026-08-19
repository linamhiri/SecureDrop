package tn.esprit.scanner.service;

import com.azure.storage.blob.BlobClient;
import com.azure.storage.blob.BlobContainerClient;
import com.azure.storage.blob.BlobServiceClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.nio.file.Path;

@Service
public class BlobDispositionService {

    private final BlobServiceClient blobServiceClient;
    private final String cleanContainerName;
    private final String infectedContainerName;

    public BlobDispositionService(
            BlobServiceClient blobServiceClient,
            @Value("${securedrop.storage.clean-container}")
            String cleanContainerName,
            @Value("${securedrop.storage.infected-container}")
            String infectedContainerName) {

        this.blobServiceClient = blobServiceClient;
        this.cleanContainerName = cleanContainerName;
        this.infectedContainerName = infectedContainerName;
    }

    public void moveToClean(
            String sourceContainerName,
            String blobName,
            Path localFile) {

        moveFile(
                sourceContainerName,
                cleanContainerName,
                blobName,
                localFile
        );
    }

    public void moveToInfected(
            String sourceContainerName,
            String blobName,
            Path localFile) {

        moveFile(
                sourceContainerName,
                infectedContainerName,
                blobName,
                localFile
        );
    }

    private void moveFile(
            String sourceContainerName,
            String destinationContainerName,
            String blobName,
            Path localFile) {

        // 1. Get the original blob from quarantine
        BlobClient sourceBlob =
                blobServiceClient
                        .getBlobContainerClient(sourceContainerName)
                        .getBlobClient(blobName);

        if (!sourceBlob.exists()) {

            throw new IllegalStateException(
                    "Source blob not found: "
                            + sourceContainerName
                            + "/"
                            + blobName
            );
        }

        // 2. Get the destination container
        BlobContainerClient destinationContainer =
                blobServiceClient
                        .getBlobContainerClient(
                                destinationContainerName
                        );

        // Local development:
        // create clean/infected container automatically if missing
        destinationContainer.createIfNotExists();

        // 3. Create destination blob client
        BlobClient destinationBlob =
                destinationContainer
                        .getBlobClient(blobName);

        // 4. Upload the file that ClamAV already scanned
        destinationBlob.uploadFromFile(
                localFile.toString(),
                true
        );

        // 5. Verify destination exists before removing quarantine copy
        if (!destinationBlob.exists()) {

            throw new IllegalStateException(
                    "Destination blob was not created: "
                            + destinationContainerName
                            + "/"
                            + blobName
            );
        }

        // 6. Remove the original from quarantine
        sourceBlob.delete();
    }
}