package pl.sitpres4.ssl2;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@SpringBootApplication
public class Ssl2Application {

	public static void main(String[] args) {

		SpringApplication.run(Ssl2Application.class, args);
	}

}
