package com.github.feffef.reactivehalspringexample.common;

import java.time.Duration;
import java.util.function.Function;

import org.springframework.http.ResponseEntity;

import com.fasterxml.jackson.databind.JsonNode;

import io.wcm.caravan.hal.resource.Link;
import reactor.core.publisher.Mono;

public interface SpringRehaRequestContext<ControllerType> {

	Link createLinkTo(Function<ControllerType, Mono<ResponseEntity<JsonNode>>> controllerCall);

	void setResponseMaxAge(Duration duration);

}
