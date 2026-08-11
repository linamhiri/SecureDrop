package tn.esprit.securedrop.service;

import com.azure.storage.queue.QueueClient;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import tn.esprit.securedrop.dto.ScanRequest;
import org.springframework.stereotype.Service;

@Service
public class ScanQueueService {

    private final QueueClient scanQueueClient;
    private final ObjectMapper objectMapper;

    public ScanQueueService(
            QueueClient scanQueueClient,
            ObjectMapper objectMapper) {

        this.scanQueueClient = scanQueueClient;
        this.objectMapper = objectMapper;
    }

    public void enqueue(ScanRequest scanRequest) {

        try {

            String message =
                    objectMapper.writeValueAsString(scanRequest);

            scanQueueClient.sendMessage(message);

        } catch (JsonProcessingException exception) {

            throw new IllegalStateException(
                    "Unable to create scan request.",
                    exception
            );
        }
    }
}