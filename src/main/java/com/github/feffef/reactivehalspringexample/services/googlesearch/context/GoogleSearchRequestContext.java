package com.github.feffef.reactivehalspringexample.services.googlesearch.context;

import java.util.function.Function;

import org.springframework.http.ResponseEntity;

import com.fasterxml.jackson.databind.JsonNode;
import com.github.feffef.reactivehalspringexample.services.googlesearch.controller.GoogleSearchController;
import com.github.feffef.reactivehalspringexample.services.googlesearch.services.GoogleSearchService;

import io.wcm.caravan.hal.resource.Link;
import reactor.core.publisher.Mono;

public interface GoogleSearchRequestContext {

	GoogleSearchService getSearchService();

	Link createLinkTo(Function<GoogleSearchController, Mono<ResponseEntity<JsonNode>>> controllerCall);

}
