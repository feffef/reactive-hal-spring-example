package com.github.feffef.reactivehalspringexample.services.performance.concurrent;

import java.util.function.Function;

import org.springframework.http.ResponseEntity;

import com.fasterxml.jackson.databind.JsonNode;
import com.github.feffef.reactivehalspringexample.api.search.SearchResult;

import io.reactivex.rxjava3.core.Observable;
import io.wcm.caravan.hal.resource.Link;
import reactor.core.publisher.Mono;

public interface ConcurrentPerformanceRequestContext {

	Link createLinkTo(Function<ConcurrentPerformanceController, Mono<ResponseEntity<JsonNode>>> controllerCall);

	Observable<SearchResult> getSearchResults(String query, int delayMs);
}
