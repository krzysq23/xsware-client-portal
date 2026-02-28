package pl.xsware;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class XCPApplication {

	public static void main(String[] args) {
		SpringApplication.run(XCPApplication.class, args);
	}

}
