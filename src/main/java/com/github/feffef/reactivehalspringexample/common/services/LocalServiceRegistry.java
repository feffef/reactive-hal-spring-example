package com.github.feffef.reactivehalspringexample.common.services;

import org.springframework.boot.web.context.WebServerInitializedEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Service;

@Service
public class LocalServiceRegistry implements ApplicationListener<WebServerInitializedEvent> {

	private int localPort;

	public String getServiceUrl(String basePath) {

		// we keep it simple for now and have all services run on the local instance
		return "http://localhost:" + localPort + basePath;
	}

	@Override
	public void onApplicationEvent(WebServerInitializedEvent event) {
		this.localPort = event.getWebServer().getPort();
	}

}
