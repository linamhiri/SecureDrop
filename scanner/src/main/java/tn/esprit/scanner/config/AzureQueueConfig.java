package tn.esprit.scanner.config;

import com.azure.storage.queue.QueueClient;
import com.azure.storage.queue.QueueClientBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AzureQueueConfig {

    @Bean
    public QueueClient scanQueueClient(
            @Value("${securedrop.storage.connection-string}") String connectionString,
            @Value("${securedrop.storage.scan-queue}") String queueName) {

        return new QueueClientBuilder()
                .connectionString(connectionString)
                .queueName(queueName)
                .buildClient();
    }
}