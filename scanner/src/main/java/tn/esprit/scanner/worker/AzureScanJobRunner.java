package tn.esprit.scanner.worker;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import tn.esprit.scanner.service.ClamAvService;

@Component
@Profile("azure")
public class AzureScanJobRunner implements ApplicationRunner {

    private static final Logger log =
            LoggerFactory.getLogger(AzureScanJobRunner.class);

    private final ScanQueueWorker scanQueueWorker;
    private final ConfigurableApplicationContext applicationContext;

    private final ClamAvService clamAvService;

    public AzureScanJobRunner(
            ScanQueueWorker scanQueueWorker,
            ClamAvService clamAvService,
            ConfigurableApplicationContext applicationContext) {

        this.scanQueueWorker = scanQueueWorker;
        this.clamAvService = clamAvService;
        this.applicationContext = applicationContext;
    }

    @Override
    public void run(ApplicationArguments args) {

        log.info("Azure Scanner Job started");

        int exitCode;

        try {

            log.info("Waiting for ClamAV...");

            clamAvService.waitUntilReady();

            log.info("ClamAV is ready");

            ProcessingOutcome outcome =
                    scanQueueWorker.processOneMessage();

            log.info(
                    "Azure Scanner Job outcome: {}",
                    outcome
            );

            exitCode =
                    outcome == ProcessingOutcome.RETRY
                            ? 1
                            : 0;

        } catch (Exception e) {

            log.error(
                    "Azure Scanner Job failed",
                    e
            );

            exitCode = 1;
        }

        final int finalExitCode = exitCode;

        int springExitCode =
                SpringApplication.exit(
                        applicationContext,
                        () -> finalExitCode
                );

        System.exit(springExitCode);
    }
}