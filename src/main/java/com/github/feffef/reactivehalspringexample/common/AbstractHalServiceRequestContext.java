package com.github.feffef.reactivehalspringexample.common;

import java.util.function.Function;

import org.springframework.hateoas.server.mvc.WebMvcLinkBuilder;
import org.springframework.http.ResponseEntity;

import com.fasterxml.jackson.databind.JsonNode;

import io.wcm.caravan.hal.microservices.api.HalApiFacade;
import io.wcm.caravan.hal.resource.Link;
import reactor.core.publisher.Mono;

public abstract class AbstractHalServiceRequestContext implements HalServiceRequestContext {

	private final HalApiFacade halApi;

	public AbstractHalServiceRequestContext(HalApiFacade halApi) {
		this.halApi = halApi;
	}

	public <T> T getEntryPoint(String uri, Class<T> halApiInterface) {
		return halApi.getEntryPoint(uri, halApiInterface);
	}

	@Override
	public void limitOutputMaxAge(int seconds) {
		halApi.limitOutputMaxAge(seconds);
	}

	@Override
	public <T> Link createLinkTo(Class<? extends T> controllerClass,
			Function<T, Mono<ResponseEntity<JsonNode>>> controllerCall) {

		T controllerDummy = WebMvcLinkBuilder.methodOn(controllerClass);

		Mono<ResponseEntity<JsonNode>> invocationResult = controllerCall.apply(controllerDummy);

		String url = WebMvcLinkBuilder.linkTo(invocationResult).toString();

		return new Link(url);
	}
}
