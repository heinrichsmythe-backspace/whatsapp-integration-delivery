package za.co.backspace.whatsappintegration;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@EnableAsync
@SpringBootApplication
public class WhatsappintegrationApplication {

	public static void main(String[] args) {
		SpringApplication.run(WhatsappintegrationApplication.class, args);
	}
}
