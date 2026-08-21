package tn.esprit.scanner.config;

import com.azure.core.credential.TokenCredential;
import com.azure.identity.DefaultAzureCredentialBuilder;
import com.azure.storage.blob.BlobServiceClient;
import com.azure.storage.blob.BlobServiceClientBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AzureBlobConfig {

    @Bean
    public TokenCredential azureCredential() {
        return new DefaultAzureCredentialBuilder()
                .build();
    }

    @Bean
    public BlobServiceClient blobServiceClient(
            @Value("${securedrop.storage.connection-string:}") String connectionString,
            @Value("${securedrop.storage.blob-endpoint:}") String blobEndpoint,
            TokenCredential azureCredential) {

        if (!connectionString.isBlank()) {
            return new BlobServiceClientBuilder()
                    .connectionString(connectionString)
                    .buildClient();
        }

        return new BlobServiceClientBuilder()
                .endpoint(blobEndpoint)
                .credential(azureCredential)
                .buildClient();
    }
}