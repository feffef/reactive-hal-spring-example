package com.github.feffef.reactivehalspringexample.common.context;

import java.time.Duration;
import java.util.function.Function;

import org.springframework.http.ResponseEntity;

import com.fasterxml.jackson.databind.JsonNode;

import io.wcm.caravan.hal.resource.Link;
import io.wcm.caravan.reha.spring.api.SpringReactorReha;
import reactor.core.publisher.Mono;

public abstract class AbstractExampleRequestContext<ControllerType> {

	private final SpringReactorReha reha;

	private final Class<? extends ControllerType> controllerClass;

	public AbstractExampleRequestContext(SpringReactorReha reha, Class<? extends ControllerType> controllerClass) {
		this.reha = reha;
		this.controllerClass = controllerClass;
	}

	public <T> T getEntryPoint(String uri, Class<T> halApiInterface) {

		return reha.getEntryPoint(uri, halApiInterface);
	}

	public void setResponseMaxAge(Duration duration) {

		reha.setResponseMaxAge(duration);
	}

	public Link createLinkTo(Function<ControllerType, Mono<ResponseEntity<JsonNode>>> controllerCall) {

		return reha.createLinkTo(controllerClass, controllerCall);
	}
}
