package com.github.feffef.reactivehalspringexample.common;

import java.time.Duration;
import java.util.function.Function;

import org.springframework.hateoas.server.mvc.WebMvcLinkBuilder;
import org.springframework.http.ResponseEntity;

import com.fasterxml.jackson.databind.JsonNode;

import io.wcm.caravan.hal.resource.Link;
import io.wcm.caravan.reha.api.Reha;
import reactor.core.publisher.Mono;

public abstract class AbstractSpringRehaRequestContext<ControllerType>
		implements SpringRehaRequestContext<ControllerType> {

	private final Reha reha;

	private final Class<? extends ControllerType> controllerClass;

	public AbstractSpringRehaRequestContext(Reha reha, Class<? extends ControllerType> controllerClass) {
		this.reha = reha;
		this.controllerClass = controllerClass;
	}

	public <T> T getEntryPoint(String uri, Class<T> halApiInterface) {
		return reha.getEntryPoint(uri, halApiInterface);
	}

	@Override
	public void setResponseMaxAge(Duration duration) {
		reha.setResponseMaxAge(duration);
	}

	@Override
	public Link createLinkTo(Function<ControllerType, Mono<ResponseEntity<JsonNode>>> controllerCall) {

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
