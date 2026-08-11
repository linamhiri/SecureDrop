package tn.esprit.securedrop.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "securedrop.azure.storage")
public class StorageProperties {

    private String accountName;
    private String containerName;
    private String scanQueueName;

    public String getAccountName() {
        return accountName;
    }

    public void setAccountName(String accountName) {
        this.accountName = accountName;
    }

    public String getContainerName() {
        return containerName;
    }

    public void setContainerName(String containerName) {
        this.containerName = containerName;
    }

    public String getScanQueueName() {
        return scanQueueName;
    }

    public void setScanQueueName(String scanQueueName) {
        this.scanQueueName = scanQueueName;
    }

    public String getBlobEndpoint() {
        return "https://" + accountName + ".blob.core.windows.net";
    }

    public String getQueueEndpoint() {
        return "https://" + accountName + ".queue.core.windows.net";
    }
}