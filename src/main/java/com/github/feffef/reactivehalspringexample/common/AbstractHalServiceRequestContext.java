package com.github.feffef.reactivehalspringexample.common;

import java.time.Duration;
import java.util.function.Function;

import org.springframework.hateoas.server.mvc.WebMvcLinkBuilder;
import org.springframework.http.ResponseEntity;

import com.fasterxml.jackson.databind.JsonNode;

import io.wcm.caravan.hal.microservices.api.Reha;
import io.wcm.caravan.hal.resource.Link;
import reactor.core.publisher.Mono;

public abstract class AbstractHalServiceRequestContext implements HalServiceRequestContext {

	private final Reha reha;

	public AbstractHalServiceRequestContext(Reha reha) {
		this.reha = reha;
	}

	public <T> T getEntryPoint(String uri, Class<T> halApiInterface) {
		return reha.getEntryPoint(uri, halApiInterface);
	}

	@Override
	public void setResponseMaxAge(Duration duration) {
		reha.setResponseMaxAge(duration);
	}

	@Override
	public <T> Link createLinkTo(Class<? extends T> controllerClass,
			Function<T, Mono<ResponseEntity<JsonNode>>> controllerCall) {

		T controllerDummy = WebMvcLinkBuilder.methodOn(controllerClass);

		Mono<ResponseEntity<JsonNode>> invocationResult = controllerCall.apply(controllerDummy);

		String url = WebMvcLinkBuilder.linkTo(invocationResult).toString();

		// FIXME: properly get the base path from the request
		if (!url.startsWith("http")) {
			url = "http://localhost:8080" + url;
		}

		return new Link(url);
	}
}
