package tn.esprit.securedrop.model;

import java.time.Instant;

public class FileMetadata {

    private String id;
    private String originalFilename;
    private String blobName;
    private String contentType;
    private long size;
    private FileStatus status;
    private Instant uploadedAt;

    public FileMetadata() {
    }

    public FileMetadata(
            String id,
            String originalFilename,
            String blobName,
            String contentType,
            long size,
            FileStatus status,
            Instant uploadedAt) {

        this.id = id;
        this.originalFilename = originalFilename;
        this.blobName = blobName;
        this.contentType = contentType;
        this.size = size;
        this.status = status;
        this.uploadedAt = uploadedAt;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getOriginalFilename() {
        return originalFilename;
    }

    public void setOriginalFilename(String originalFilename) {
        this.originalFilename = originalFilename;
    }

    public String getBlobName() {
        return blobName;
    }

    public void setBlobName(String blobName) {
        this.blobName = blobName;
    }

    public String getContentType() {
        return contentType;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    public long getSize() {
        return size;
    }

    public void setSize(long size) {
        this.size = size;
    }

    public FileStatus getStatus() {
        return status;
    }

    public void setStatus(FileStatus status) {
        this.status = status;
    }

    public Instant getUploadedAt() {
        return uploadedAt;
    }

    public void setUploadedAt(Instant uploadedAt) {
        this.uploadedAt = uploadedAt;
    }
}