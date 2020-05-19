package com.github.feffef.reactivehalspringexample;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class ReactiveHalSpringExampleApplication {

	public static void main(String[] args) {

		// FIXME: set these externally with configuration
		System.setProperty("server.tomcat.max-threads", "5");

		SpringApplication.run(ReactiveHalSpringExampleApplication.class, args);
	}

}
