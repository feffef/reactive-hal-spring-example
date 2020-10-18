package io.wcm.caravan.reha.spring.api;

import java.time.Duration;
import java.util.function.Function;

import org.springframework.http.ResponseEntity;
import org.springframework.web.context.request.ServletWebRequest;

import com.fasterxml.jackson.databind.JsonNode;

import io.wcm.caravan.hal.resource.Link;
import reactor.core.publisher.Mono;

public interface SpringReactorReha {

	<T> T getUpstreamEntryPoint(String uri, Class<T> halApiInterface);

	void setResponseMaxAge(Duration duration);

	<ControllerType> Link createLinkTo(Class<? extends ControllerType> controllerClass,
			Function<ControllerType, Mono<ResponseEntity<JsonNode>>> controllerCall);

	ServletWebRequest getRequest();

}
