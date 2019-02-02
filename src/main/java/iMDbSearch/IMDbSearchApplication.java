package iMDbSearch;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;


@SpringBootApplication
@EnableScheduling
public class IMDbSearchApplication {

	public static void main(String[] args) {
		SpringApplication.run(IMDbSearchApplication.class, args);
	
	}

}

