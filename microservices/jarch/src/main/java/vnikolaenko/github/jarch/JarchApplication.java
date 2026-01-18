package vnikolaenko.github.jarch;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;

@SpringBootApplication()
@EntityScan("vnikolaenko.github.jarch.model")
public class JarchApplication {

	public static void main(String[] args) {
		SpringApplication.run(JarchApplication.class, args);
	}

}
