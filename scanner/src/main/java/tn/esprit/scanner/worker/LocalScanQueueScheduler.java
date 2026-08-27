package tn.esprit.scanner.worker;

import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@Profile("local")
public class LocalScanQueueScheduler {

    private final ScanQueueWorker scanQueueWorker;

    public LocalScanQueueScheduler(ScanQueueWorker scanQueueWorker) {
        this.scanQueueWorker = scanQueueWorker;
    }

    @Scheduled(fixedDelayString = "${scanner.poll-delay}")
    public void pollQueue() {
        scanQueueWorker.processOneMessage();
    }
}