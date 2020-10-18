package com.github.feffef.reactivehalspringexample;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan(basePackages = { "io.wcm.caravan.rhyme.spring", "com.github.feffef.reactivehalspringexample" })
public class ReactiveHalSpringExampleApplication {

	public static void main(String[] args) {

		// FIXME: set these externally with configuration
		System.setProperty("server.tomcat.max-threads", "5");

		SpringApplication.run(ReactiveHalSpringExampleApplication.class, args);
	}

}
