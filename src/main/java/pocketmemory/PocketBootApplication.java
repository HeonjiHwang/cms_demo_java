package pocketmemory;

import org.springframework.boot.Banner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.ServletComponentScan;
import org.springframework.context.annotation.Import;

import pocketmemory.egov.com.config.EgovWebApplicationInitializer;

@ServletComponentScan
@SpringBootApplication
@Import({EgovWebApplicationInitializer.class})
public class PocketBootApplication {
	public static void main(String[] args) {
		System.out.println("##### PocketBootApplication Start #####");

		SpringApplication springApplication = new SpringApplication(PocketBootApplication.class);
		springApplication.setBannerMode(Banner.Mode.OFF);
		//springApplication.setLogStartupInfo(false);
		springApplication.run(args);

		System.out.println("##### PocketBootApplication End #####");
	}

}
