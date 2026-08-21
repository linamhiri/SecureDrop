package tn.esprit.scanner.config;

import com.azure.core.credential.TokenCredential;
import com.azure.data.tables.TableClient;
import com.azure.data.tables.TableServiceClient;
import com.azure.data.tables.TableServiceClientBuilder;
import com.azure.identity.DefaultAzureCredentialBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AzureTableConfig {

    @Bean
    public TableServiceClient tableServiceClient(
            @Value("${securedrop.storage.connection-string:}") String connectionString,
            @Value("${securedrop.storage.table-endpoint:}") String tableEndpoint,
            TokenCredential azureCredential) {

        if (!connectionString.isBlank()) {
            return new TableServiceClientBuilder()
                    .connectionString(connectionString)
                    .buildClient();
        }

        return new TableServiceClientBuilder()
                .endpoint(tableEndpoint)
                .credential(azureCredential)
                .buildClient();
    }

    @Bean
    public TableClient metadataTableClient(
            TableServiceClient tableServiceClient,
            @Value("${securedrop.storage.metadata-table}") String tableName,
            @Value("${spring.profiles.active:local}") String activeProfile) {

        TableClient tableClient =
                tableServiceClient.getTableClient(tableName);

        if ("local".equals(activeProfile)) {
            tableServiceClient.createTableIfNotExists(tableName);
        }

        return tableClient;
    }
}