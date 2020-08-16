package io.wcm.caravan.reha.spring.api;

import java.util.function.Function;

import org.springframework.http.ResponseEntity;

import com.fasterxml.jackson.databind.JsonNode;

import io.wcm.caravan.reha.api.resources.LinkableResource;
import reactor.core.publisher.Mono;

public interface SpringRehaAsyncRequestProcessor {

	<RequestContextType> Mono<ResponseEntity<JsonNode>> processRequest(
			Function<SpringReactorReha, RequestContextType> requestContextConstructor,
			Function<RequestContextType, LinkableResource> resourceImplConstructor);

}