package tn.esprit.securedrop.dto;

import java.time.Instant;

public class ScanRequest {

    private String fileId;
    private String blobName;
    private String containerName;
    private Instant requestedAt;

    public ScanRequest() {
    }

    public ScanRequest(
            String fileId,
            String blobName,
            String containerName,
            Instant requestedAt) {

        this.fileId = fileId;
        this.blobName = blobName;
        this.containerName = containerName;
        this.requestedAt = requestedAt;
    }

    public String getFileId() {
        return fileId;
    }

    public void setFileId(String fileId) {
        this.fileId = fileId;
    }

    public String getBlobName() {
        return blobName;
    }

    public void setBlobName(String blobName) {
        this.blobName = blobName;
    }

    public String getContainerName() {
        return containerName;
    }

    public void setContainerName(String containerName) {
        this.containerName = containerName;
    }

    public Instant getRequestedAt() {
        return requestedAt;
    }

    public void setRequestedAt(Instant requestedAt) {
        this.requestedAt = requestedAt;
    }
}