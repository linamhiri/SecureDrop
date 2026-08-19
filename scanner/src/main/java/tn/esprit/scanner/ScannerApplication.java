package tn.esprit.scanner;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SpringBootApplication
public class ScannerApplication {

	public static void main(String[] args) {
		SpringApplication.run(ScannerApplication.class, args);
	}
}
