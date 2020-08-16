package com.github.feffef.reactivehalspringexample.services.metasearch.context;

import java.util.function.Function;

import org.springframework.http.ResponseEntity;

import com.fasterxml.jackson.databind.JsonNode;
import com.github.feffef.reactivehalspringexample.api.search.SearchOptions;
import com.github.feffef.reactivehalspringexample.api.search.SearchResult;
import com.github.feffef.reactivehalspringexample.services.metasearch.controller.MetaSearchController;

import io.reactivex.rxjava3.core.Flowable;
import io.wcm.caravan.hal.resource.Link;
import reactor.core.publisher.Mono;

public interface MetaSearchRequestContext {

	Link createLinkTo(Function<MetaSearchController, Mono<ResponseEntity<JsonNode>>> controllerCall);

	Flowable<SearchResult> getResultsFromFirst(String query, SearchOptions metaOptions);

	Flowable<SearchResult> getGoogleResults(String query, SearchOptions metaOptions);

	Flowable<SearchResult> merge(Flowable<SearchResult> exampleResults, Flowable<SearchResult> googleResults);

	Flowable<SearchResult> getResultsFromSecond(String query, SearchOptions metaOptions);
}
