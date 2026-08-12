package tn.esprit.securedrop.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "securedrop.azure.storage")
public class StorageProperties {

    private String connectionString;
    private String accountName;

    private String quarantineContainer;
    private String cleanContainer;
    private String infectedContainer;

    private String scanQueueName;
    private String metadataTableName;


    // =========================
    // CONNECTION
    // =========================

    public String getConnectionString() {
        return connectionString;
    }

    public void setConnectionString(String connectionString) {
        this.connectionString = connectionString;
    }

    public String getAccountName() {
        return accountName;
    }

    public void setAccountName(String accountName) {
        this.accountName = accountName;
    }


    // =========================
    // BLOB CONTAINERS
    // =========================

    public String getQuarantineContainer() {
        return quarantineContainer;
    }

    public void setQuarantineContainer(String quarantineContainer) {
        this.quarantineContainer = quarantineContainer;
    }

    public String getCleanContainer() {
        return cleanContainer;
    }

    public void setCleanContainer(String cleanContainer) {
        this.cleanContainer = cleanContainer;
    }

    public String getInfectedContainer() {
        return infectedContainer;
    }

    public void setInfectedContainer(String infectedContainer) {
        this.infectedContainer = infectedContainer;
    }


    // =========================
    // QUEUE
    // =========================

    public String getScanQueueName() {
        return scanQueueName;
    }

    public void setScanQueueName(String scanQueueName) {
        this.scanQueueName = scanQueueName;
    }


    // =========================
    // TABLE
    // =========================

    public String getMetadataTableName() {
        return metadataTableName;
    }

    public void setMetadataTableName(String metadataTableName) {
        this.metadataTableName = metadataTableName;
    }


    // =========================
    // LOCAL / AZURE
    // =========================

    public boolean isLocal() {
        return connectionString != null
                && !connectionString.isBlank();
    }


    // =========================
    // REAL AZURE ENDPOINTS
    // =========================

    public String getBlobEndpoint() {
        return "https://" + accountName
                + ".blob.core.windows.net";
    }

    public String getQueueEndpoint() {
        return "https://" + accountName
                + ".queue.core.windows.net";
    }

    public String getTableEndpoint() {
        return "https://" + accountName
                + ".table.core.windows.net";
    }
}