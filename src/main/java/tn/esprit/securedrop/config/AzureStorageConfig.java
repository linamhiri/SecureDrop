package tn.esprit.securedrop.config;

import com.azure.core.credential.TokenCredential;
import com.azure.data.tables.TableClient;
import com.azure.data.tables.TableServiceClient;
import com.azure.data.tables.TableServiceClientBuilder;
import com.azure.identity.DefaultAzureCredentialBuilder;
import com.azure.storage.blob.BlobContainerClient;
import com.azure.storage.blob.BlobServiceClient;
import com.azure.storage.blob.BlobServiceClientBuilder;
import com.azure.storage.queue.QueueClient;
import com.azure.storage.queue.QueueClientBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AzureStorageConfig {

    /*
     * Used later when running against real Azure.
     *
     * Locally, Azurite uses its development connection string instead.
     */
    @Bean
    public TokenCredential azureCredential() {

        return new DefaultAzureCredentialBuilder()
                .build();
    }


    // =========================================================
    // BLOB STORAGE
    // =========================================================

    @Bean
    public BlobServiceClient blobServiceClient(
            StorageProperties properties,
            TokenCredential azureCredential) {

        if (properties.isLocal()) {

            return new BlobServiceClientBuilder()
                    .connectionString(
                            properties.getConnectionString()
                    )
                    .buildClient();
        }

        return new BlobServiceClientBuilder()
                .endpoint(properties.getBlobEndpoint())
                .credential(azureCredential)
                .buildClient();
    }


    @Bean
    public BlobContainerClient blobContainerClient(
            BlobServiceClient blobServiceClient,
            StorageProperties properties) {

        BlobContainerClient client =
                blobServiceClient.getBlobContainerClient(
                        properties.getQuarantineContainer()
                );

        /*
         * During local development we automatically create
         * the Azurite container.
         *
         * In Azure, resources will be provisioned explicitly.
         */
        if (properties.isLocal()) {
            client.createIfNotExists();
        }

        return client;
    }


    // =========================================================
    // QUEUE STORAGE
    // =========================================================

    @Bean
    public QueueClient scanQueueClient(
            StorageProperties properties,
            TokenCredential azureCredential) {

        QueueClient client;

        if (properties.isLocal()) {

            client = new QueueClientBuilder()
                    .connectionString(
                            properties.getConnectionString()
                    )
                    .queueName(
                            properties.getScanQueueName()
                    )
                    .buildClient();

        } else {

            client = new QueueClientBuilder()
                    .endpoint(
                            properties.getQueueEndpoint()
                    )
                    .queueName(
                            properties.getScanQueueName()
                    )
                    .credential(azureCredential)
                    .buildClient();
        }

        if (properties.isLocal()) {
            client.createIfNotExists();
        }

        return client;
    }


    // =========================================================
    // TABLE STORAGE
    // =========================================================

    @Bean
    public TableServiceClient tableServiceClient(
            StorageProperties properties,
            TokenCredential azureCredential) {

        if (properties.isLocal()) {

            return new TableServiceClientBuilder()
                    .connectionString(
                            properties.getConnectionString()
                    )
                    .buildClient();
        }

        return new TableServiceClientBuilder()
                .endpoint(properties.getTableEndpoint())
                .credential(azureCredential)
                .buildClient();
    }


    @Bean
    public TableClient metadataTableClient(
            TableServiceClient tableServiceClient,
            StorageProperties properties) {

        if (properties.isLocal()) {

            return tableServiceClient
                    .createTableIfNotExists(
                            properties.getMetadataTableName()
                    );
        }

        return tableServiceClient
                .getTableClient(
                        properties.getMetadataTableName()
                );
    }
}