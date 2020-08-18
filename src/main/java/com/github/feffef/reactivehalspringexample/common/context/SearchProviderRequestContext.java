package com.github.feffef.reactivehalspringexample.common.context;

import java.util.Optional;
import java.util.function.Function;

import org.springframework.http.ResponseEntity;

import com.fasterxml.jackson.databind.JsonNode;
import com.github.feffef.reactivehalspringexample.common.controller.SearchProviderController;
import com.github.feffef.reactivehalspringexample.common.services.SearchResultProvider;

import io.wcm.caravan.hal.resource.Link;
import reactor.core.publisher.Mono;

public interface SearchProviderRequestContext {

	Link createLinkTo(Function<SearchProviderController, Mono<ResponseEntity<JsonNode>>> controllerCall);

	SearchResultProvider getSearchResultProvider();

	Optional<String> getQueryTimestamp();
}
