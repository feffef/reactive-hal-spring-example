package com.github.feffef.reactivehalspringexample.common;

import java.util.function.Function;

import org.springframework.hateoas.server.mvc.WebMvcLinkBuilder;
import org.springframework.http.ResponseEntity;

import com.fasterxml.jackson.databind.JsonNode;

import io.wcm.caravan.hal.microservices.api.HalApiFacade;
import io.wcm.caravan.hal.resource.Link;
import reactor.core.publisher.Mono;

public abstract class AbstractHalServiceRequestContext<ControllerClass> {

	private final HalApiFacade halApi;

	public AbstractHalServiceRequestContext(HalApiFacade halApi) {
		this.halApi = halApi;
	}

	protected abstract Class<? extends ControllerClass> getControllerClass();

	public <T> T getEntryPoint(String uri, Class<T> halApiInterface) {
		return halApi.getEntryPoint(uri, halApiInterface);
	}

	public void limitOutputMaxAge(int seconds) {
		halApi.limitOutputMaxAge(seconds);
	}

	public Link createLinkTo(Function<ControllerClass, Mono<ResponseEntity<JsonNode>>> controllerCall) {

		Class<? extends ControllerClass> controllerClass = getControllerClass();

		ControllerClass controllerDummy = WebMvcLinkBuilder.methodOn(controllerClass);

		Mono<ResponseEntity<JsonNode>> invocationResult = controllerCall.apply(controllerDummy);

		String url = WebMvcLinkBuilder.linkTo(invocationResult).toString();

		return new Link(url);
	}
}
