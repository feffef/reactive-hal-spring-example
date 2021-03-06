package com.github.feffef.reactivehalspringexample.common.controller;

import java.util.function.Function;

import org.springframework.http.ResponseEntity;

import com.fasterxml.jackson.databind.JsonNode;

import reactor.core.publisher.Mono;

public interface SearchProviderController {

	Mono<ResponseEntity<JsonNode>> getEntryPoint(String queryTimestamp);

	Mono<ResponseEntity<JsonNode>> getResultPage(String query, Integer delayMs, Integer startIndex);

	interface HandlerMethodCall extends Function<SearchProviderController, Mono<ResponseEntity<JsonNode>>> {

	}
}