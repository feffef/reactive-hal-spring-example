package com.github.feffef.reactivehalspringexample.common;

import java.util.function.Function;

import org.springframework.http.ResponseEntity;

import com.fasterxml.jackson.databind.JsonNode;

import io.wcm.caravan.hal.resource.Link;
import reactor.core.publisher.Mono;

public interface HalServiceRequestContext {

	<T> Link createLinkTo(Class<? extends T> controllerClass,
			Function<T, Mono<ResponseEntity<JsonNode>>> controllerCall);

	void limitOutputMaxAge(int seconds);
}
