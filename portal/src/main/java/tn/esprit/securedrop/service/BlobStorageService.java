package tn.esprit.securedrop.service;

import com.azure.core.util.BinaryData;
import com.azure.storage.blob.BlobClient;
import com.azure.storage.blob.BlobContainerClient;
import com.azure.storage.blob.models.BlobHttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@Service
public class BlobStorageService {

    private final BlobContainerClient blobContainerClient;

    public BlobStorageService(
            BlobContainerClient blobContainerClient) {

        this.blobContainerClient = blobContainerClient;
    }

    public void upload(
            MultipartFile file,
            String blobName) {

        BlobClient blobClient =
                blobContainerClient.getBlobClient(blobName);

        try {

            BinaryData data =
                    BinaryData.fromBytes(file.getBytes());

            blobClient.upload(data, true);

            if (file.getContentType() != null
                    && !file.getContentType().isBlank()) {

                BlobHttpHeaders headers =
                        new BlobHttpHeaders()
                                .setContentType(
                                        file.getContentType()
                                );

                blobClient.setHttpHeaders(headers);
            }

        } catch (IOException exception) {

            throw new IllegalStateException(
                    "Unable to read the uploaded file.",
                    exception
            );
        }
    }
}