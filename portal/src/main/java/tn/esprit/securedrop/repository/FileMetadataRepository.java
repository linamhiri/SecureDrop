package tn.esprit.securedrop.repository;

import com.azure.data.tables.TableClient;
import com.azure.data.tables.models.TableEntity;
import com.azure.data.tables.models.TableEntityUpdateMode;
import com.azure.data.tables.models.TableServiceException;
import org.springframework.stereotype.Repository;
import tn.esprit.securedrop.model.FileMetadata;
import tn.esprit.securedrop.model.FileStatus;

import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

@Repository
public class FileMetadataRepository {

    private static final String PARTITION_KEY = "files";

    private final TableClient tableClient;

    public FileMetadataRepository(TableClient tableClient) {
        this.tableClient = tableClient;
    }

    public FileMetadata save(FileMetadata fileMetadata) {

        TableEntity entity = toTableEntity(fileMetadata);

        tableClient.upsertEntityWithResponse(
                entity,
                TableEntityUpdateMode.REPLACE,
                null,
                null
        );

        return fileMetadata;
    }

    public Optional<FileMetadata> findById(String id) {

        try {

            TableEntity entity =
                    tableClient.getEntity(
                            PARTITION_KEY,
                            id
                    );

            return Optional.of(
                    fromTableEntity(entity)
            );

        } catch (TableServiceException exception) {

            if (exception.getResponse() != null
                    && exception.getResponse().getStatusCode() == 404) {

                return Optional.empty();
            }

            throw exception;
        }
    }

    public List<FileMetadata> findAll() {

        return tableClient
                .listEntities()
                .stream()
                .filter(entity ->
                        PARTITION_KEY.equals(
                                entity.getPartitionKey()
                        )
                )
                .map(this::fromTableEntity)
                .sorted(
                        Comparator
                                .comparing(
                                        FileMetadata::getUploadedAt
                                )
                                .reversed()
                )
                .toList();
    }

    public void deleteById(String id) {

        try {

            tableClient.deleteEntity(
                    PARTITION_KEY,
                    id
            );

        } catch (TableServiceException exception) {

            if (exception.getResponse() == null
                    || exception.getResponse().getStatusCode() != 404) {

                throw exception;
            }
        }
    }

    private TableEntity toTableEntity(
            FileMetadata fileMetadata) {

        TableEntity entity =
                new TableEntity(
                        PARTITION_KEY,
                        fileMetadata.getId()
                );

        entity.addProperty(
                "originalFilename",
                fileMetadata.getOriginalFilename()
        );

        entity.addProperty(
                "blobName",
                fileMetadata.getBlobName()
        );

        if (fileMetadata.getContentType() != null) {

            entity.addProperty(
                    "contentType",
                    fileMetadata.getContentType()
            );
        }

        entity.addProperty(
                "size",
                fileMetadata.getSize()
        );

        entity.addProperty(
                "status",
                fileMetadata.getStatus().name()
        );

        entity.addProperty(
                "uploadedAt",
                fileMetadata.getUploadedAt().toString()
        );

        return entity;
    }

    private FileMetadata fromTableEntity(
            TableEntity entity) {

        FileMetadata metadata =
                new FileMetadata();

        metadata.setId(
                entity.getRowKey()
        );

        metadata.setOriginalFilename(
                (String) entity.getProperty(
                        "originalFilename"
                )
        );

        metadata.setBlobName(
                (String) entity.getProperty(
                        "blobName"
                )
        );

        metadata.setContentType(
                (String) entity.getProperty(
                        "contentType"
                )
        );

        Object size =
                entity.getProperty("size");

        if (size instanceof Number number) {
            metadata.setSize(
                    number.longValue()
            );
        }

        String status =
                (String) entity.getProperty(
                        "status"
                );

        metadata.setStatus(
                FileStatus.valueOf(status)
        );

        String uploadedAt =
                (String) entity.getProperty(
                        "uploadedAt"
                );

        metadata.setUploadedAt(
                Instant.parse(uploadedAt)
        );

        return metadata;
    }
}