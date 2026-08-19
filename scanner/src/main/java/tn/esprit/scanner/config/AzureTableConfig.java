package tn.esprit.scanner.config;

import com.azure.data.tables.TableClient;
import com.azure.data.tables.TableServiceClient;
import com.azure.data.tables.TableServiceClientBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AzureTableConfig {

    @Bean
    public TableServiceClient tableServiceClient(
            @Value("${securedrop.storage.connection-string}")
            String connectionString) {

        return new TableServiceClientBuilder()
                .connectionString(connectionString)
                .buildClient();
    }

    @Bean
    public TableClient metadataTableClient(
            TableServiceClient tableServiceClient,
            @Value("${securedrop.storage.metadata-table}")
            String tableName) {

        return tableServiceClient
                .createTableIfNotExists(tableName);
    }
}