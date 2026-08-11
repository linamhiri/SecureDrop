package tn.esprit.securedrop.config;

import com.azure.core.credential.TokenCredential;
import com.azure.identity.DefaultAzureCredentialBuilder;
import com.azure.storage.blob.BlobContainerClient;
import com.azure.storage.blob.BlobServiceClientBuilder;
import com.azure.storage.queue.QueueClient;
import com.azure.storage.queue.QueueClientBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AzureStorageConfig {

    @Bean
    public TokenCredential azureCredential() {
        return new DefaultAzureCredentialBuilder()
                .build();
    }

    @Bean
    public BlobContainerClient blobContainerClient(
            StorageProperties properties,
            TokenCredential azureCredential) {

        return new BlobServiceClientBuilder()
                .endpoint(properties.getBlobEndpoint())
                .credential(azureCredential)
                .buildClient()
                .getBlobContainerClient(properties.getContainerName());
    }

    @Bean
    public QueueClient scanQueueClient(
            StorageProperties properties,
            TokenCredential azureCredential) {

        return new QueueClientBuilder()
                .endpoint(properties.getQueueEndpoint())
                .queueName(properties.getScanQueueName())
                .credential(azureCredential)
                .buildClient();
    }
}