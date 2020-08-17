package io.wcm.caravan.reha.spring.impl;

import java.time.Duration;
import java.util.function.Function;

import org.springframework.hateoas.server.mvc.WebMvcLinkBuilder;
import org.springframework.http.ResponseEntity;
import org.springframework.web.context.request.ServletWebRequest;

import com.fasterxml.jackson.databind.JsonNode;

import io.wcm.caravan.hal.resource.Link;
import io.wcm.caravan.reha.api.Reha;
import io.wcm.caravan.reha.spring.api.SpringReactorReha;
import reactor.core.publisher.Mono;

final class SpringReactorRehaImpl implements SpringReactorReha {

	private final Reha reha;
	private final ServletWebRequest webRequest;

	SpringReactorRehaImpl(Reha reha, ServletWebRequest webRequest) {
		this.reha = reha;
		this.webRequest = webRequest;
	}

	@Override
	public ServletWebRequest getWebRequest() {
		return webRequest;
	}

	@Override
	public <T> T getEntryPoint(String uri, Class<T> halApiInterface) {
		return reha.getEntryPoint(uri, halApiInterface);
	}

	@Override
	public void setResponseMaxAge(Duration duration) {
		reha.setResponseMaxAge(duration);
	}

	@Override
	public <ControllerType> Link createLinkTo(Class<? extends ControllerType> controllerClass,
			Function<ControllerType, Mono<ResponseEntity<JsonNode>>> controllerCall) {

		ControllerType controllerDummy = WebMvcLinkBuilder.methodOn(controllerClass);

		Mono<ResponseEntity<JsonNode>> invocationResult = controllerCall.apply(controllerDummy);

		String url = WebMvcLinkBuilder.linkTo(invocationResult).toString();

		// FIXME: properly get the base path from the request
		if (!url.startsWith("http")) {
			url = "http://localhost:8080" + url;
		}

		return new Link(url);
	}
}