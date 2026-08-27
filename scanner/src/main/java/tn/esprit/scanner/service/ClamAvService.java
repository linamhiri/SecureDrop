package tn.esprit.scanner.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.*;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

@Service
public class ClamAvService {

    private static final int CHUNK_SIZE = 8192;

    private final String host;
    private final int port;
    private final int connectTimeout;
    private final int readTimeout;

    public ClamAvService(
            @Value("${scanner.clamav.host}") String host,
            @Value("${scanner.clamav.port}") int port,
            @Value("${scanner.clamav.connect-timeout}") int connectTimeout,
            @Value("${scanner.clamav.read-timeout}") int readTimeout) {

        this.host = host;
        this.port = port;
        this.connectTimeout = connectTimeout;
        this.readTimeout = readTimeout;
    }

    public void waitUntilReady() throws InterruptedException {

        int maxAttempts = 30;
        long delayMillis = 2000;

        for (int attempt = 1; attempt <= maxAttempts; attempt++) {

            try (Socket socket = new Socket()) {

                socket.connect(
                        new InetSocketAddress(host, port),
                        connectTimeout
                );

                socket.setSoTimeout(readTimeout);

                OutputStream output = socket.getOutputStream();

                output.write(
                        "zPING\0"
                                .getBytes(StandardCharsets.US_ASCII)
                );

                output.flush();

                String response =
                        readResponse(socket.getInputStream());

                if ("PONG".equalsIgnoreCase(response.trim())) {
                    return;
                }

            } catch (IOException e) {
                // ClamAV may still be loading its signature database
            }

            Thread.sleep(delayMillis);
        }

        throw new IllegalStateException(
                "ClamAV did not become ready after 60 seconds"
        );
    }

    public ScanResult scan(Path file) throws IOException {

        if (file == null || !Files.exists(file)) {
            throw new IllegalArgumentException("File does not exist: " + file);
        }

        try (Socket socket = new Socket()) {

            socket.connect(
                    new InetSocketAddress(host, port),
                    connectTimeout
            );

            socket.setSoTimeout(readTimeout);

            try (
                    DataOutputStream output =
                            new DataOutputStream(
                                    new BufferedOutputStream(
                                            socket.getOutputStream()
                                    )
                            );

                    InputStream fileInput =
                            new BufferedInputStream(
                                    Files.newInputStream(file)
                            )
            ) {

                /*
                 * zINSTREAM\0
                 *
                 * z = command uses a NULL terminator
                 * INSTREAM = send file contents through socket
                 */
                output.write(
                        "zINSTREAM\0"
                                .getBytes(StandardCharsets.US_ASCII)
                );

                byte[] buffer = new byte[CHUNK_SIZE];

                int bytesRead;

                while ((bytesRead = fileInput.read(buffer)) != -1) {

                    // DataOutputStream.writeInt writes 4-byte big-endian integer
                    output.writeInt(bytesRead);

                    output.write(
                            buffer,
                            0,
                            bytesRead
                    );
                }

                /*
                 * Zero-length chunk tells clamd
                 * that the file stream has finished.
                 */
                output.writeInt(0);

                output.flush();

                String response =
                        readResponse(socket.getInputStream());

                return parseResponse(response);
            }
        }
    }

    private String readResponse(InputStream inputStream)
            throws IOException {

        ByteArrayOutputStream response =
                new ByteArrayOutputStream();

        int currentByte;

        while ((currentByte = inputStream.read()) != -1) {

            // z-framed ClamAV responses terminate with NULL
            if (currentByte == 0) {
                break;
            }

            response.write(currentByte);
        }

        return response.toString(StandardCharsets.UTF_8);
    }

    private ScanResult parseResponse(String response) {

        if (response == null || response.isBlank()) {
            throw new IllegalStateException(
                    "ClamAV returned an empty response"
            );
        }

        if (response.endsWith(" OK")) {
            return new ScanResult(
                    ScanStatus.CLEAN,
                    null,
                    response
            );
        }

        if (response.endsWith(" FOUND")) {

            String virusName = extractVirusName(response);

            return new ScanResult(
                    ScanStatus.INFECTED,
                    virusName,
                    response
            );
        }

        return new ScanResult(
                ScanStatus.ERROR,
                null,
                response
        );
    }

    private String extractVirusName(String response) {

        int colonIndex = response.indexOf(':');
        int foundIndex = response.lastIndexOf(" FOUND");

        if (colonIndex == -1 ||
                foundIndex == -1 ||
                foundIndex <= colonIndex) {

            return "Unknown";
        }

        return response
                .substring(
                        colonIndex + 1,
                        foundIndex
                )
                .trim();
    }

    public enum ScanStatus {
        CLEAN,
        INFECTED,
        ERROR
    }

    public record ScanResult(
            ScanStatus status,
            String virusName,
            String rawResponse
    ) {
    }
}