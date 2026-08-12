package tn.esprit.securedrop.service;

import com.azure.storage.queue.QueueClient;
import org.springframework.stereotype.Service;
import tn.esprit.securedrop.dto.ScanRequest;
import tools.jackson.databind.json.JsonMapper;

@Service
public class ScanQueueService {

    private final QueueClient scanQueueClient;
    private final JsonMapper jsonMapper;

    public ScanQueueService(
            QueueClient scanQueueClient,
            JsonMapper jsonMapper) {

        this.scanQueueClient = scanQueueClient;
        this.jsonMapper = jsonMapper;
    }

    public void enqueue(ScanRequest scanRequest) {

        try {

            String message =
                    jsonMapper.writeValueAsString(scanRequest);

            scanQueueClient.sendMessage(message);

        } catch (Exception exception) {

            throw new IllegalStateException(
                    "Unable to create or send scan request.",
                    exception
            );
        }
    }
}