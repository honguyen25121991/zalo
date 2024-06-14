package com.zalo.Spring_Zalo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;

@SpringBootApplication(scanBasePackages = "com.zalo.Spring_Zalo")
public class SpringZaloApplication extends SpringBootServletInitializer {
	public static void main(String[] args) {
		SpringApplication.run(SpringZaloApplication.class, args);
	}

}
