package io.wcm.caravan.reha.spring.api;

import java.time.Duration;
import java.util.function.Function;

import org.springframework.http.ResponseEntity;

import com.fasterxml.jackson.databind.JsonNode;

import io.wcm.caravan.hal.resource.Link;
import reactor.core.publisher.Mono;

public interface SpringReactorReha {

	<T> T getEntryPoint(String uri, Class<T> halApiInterface);

	void setResponseMaxAge(Duration duration);

	<ControllerType> Link createLinkTo(Class<? extends ControllerType> controllerClass,
			Function<ControllerType, Mono<ResponseEntity<JsonNode>>> controllerCall);

}
