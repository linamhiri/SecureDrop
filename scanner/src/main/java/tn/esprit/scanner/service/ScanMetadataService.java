package tn.esprit.scanner.service;

import com.azure.data.tables.TableClient;
import com.azure.data.tables.models.TableEntity;
import com.azure.data.tables.models.TableEntityUpdateMode;
import org.springframework.stereotype.Service;

@Service
public class ScanMetadataService {

    private static final String PARTITION_KEY = "files";

    private final TableClient tableClient;

    public ScanMetadataService(TableClient tableClient) {
        this.tableClient = tableClient;
    }

    public void updateStatus(
            String fileId,
            String status) {

        TableEntity entity =
                tableClient.getEntity(
                        PARTITION_KEY,
                        fileId
                );

        entity.addProperty(
                "status",
                status
        );

        tableClient.updateEntity(
                entity,
                TableEntityUpdateMode.MERGE
        );
    }
}