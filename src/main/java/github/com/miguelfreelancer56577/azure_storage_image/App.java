package github.com.miguelfreelancer56577.azure_storage_image;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.reactive.config.EnableWebFlux;

import lombok.extern.slf4j.Slf4j;

/**
 * Starter point to run String boot application
 */
@SpringBootApplication
@EnableWebFlux
@Slf4j
public class App
{
	public static void main( String[] args )
	{
		log.info("Running spring boot applicastion: " + App.class.getName());
		SpringApplication.run(App.class, args);
	}
}
