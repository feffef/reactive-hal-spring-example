package com.github.feffef.reactivehalspringexample.services.performance.concurrent;

import java.util.function.Function;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.databind.JsonNode;
import com.github.feffef.reactivehalspringexample.api.search.SearchEntryPointResource;
import com.github.feffef.reactivehalspringexample.api.search.SearchOptions;
import com.github.feffef.reactivehalspringexample.api.search.SearchResult;
import com.github.feffef.reactivehalspringexample.api.search.SearchResultPageResource;
import com.github.feffef.reactivehalspringexample.api.search.SearchResultResource;
import com.github.feffef.reactivehalspringexample.common.context.AbstractExampleRequestContext;
import com.github.feffef.reactivehalspringexample.common.services.LocalServiceRegistry;
import com.github.feffef.reactivehalspringexample.services.metasearch.MetaSearchController;

import io.reactivex.rxjava3.core.Observable;
import io.wcm.caravan.rhyme.api.resources.LinkableResource;
import io.wcm.caravan.rhyme.spring.api.SpringReactorRhyme;
import io.wcm.caravan.rhyme.spring.api.SpringRhymeAsyncRequestProcessor;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping(ConcurrentPerformanceController.BASE_PATH)
public class ConcurrentPerformanceController {

	public static final String BASE_PATH = "/performance/concurrent";

	@Autowired
	private SpringRhymeAsyncRequestProcessor asyncProcessor;

	@Autowired
	private LocalServiceRegistry serviceRegistry;

	@GetMapping()
	public Mono<ResponseEntity<JsonNode>> getEntryPoint() {

		return renderResource(request -> new ConcurrentPerformanceEntryPointResource(request));
	}

	@GetMapping("/result")
	public Mono<ResponseEntity<JsonNode>> getResult(@RequestParam("numRequests") Integer numRequests,
			@RequestParam("delayMs") Integer delayMs) {

		return renderResource(request -> new ConcurrentPerformanceResultResource(request, numRequests, delayMs));
	}

	Mono<ResponseEntity<JsonNode>> renderResource(
			Function<ConcurrentPerformanceRequestContext, LinkableResource> resourceConstructor) {

		return asyncProcessor.processRequest(RequestContext::new, resourceConstructor);
	}

	class RequestContext extends AbstractExampleRequestContext<ConcurrentPerformanceController>
			implements ConcurrentPerformanceRequestContext {

		RequestContext(SpringReactorRhyme rhyme) {
			super(rhyme, ConcurrentPerformanceController.class);
		}

		@Override
		public Observable<SearchResult> getSearchResults(String query, int delayMs) {

			String entryPointUri = serviceRegistry.getServiceUrl(MetaSearchController.BASE_PATH);

			SearchEntryPointResource searchEntryPoint = getEntryPoint(entryPointUri, SearchEntryPointResource.class);

			SearchOptions options = new SearchOptions();
			options.delayMs = delayMs;
			options.skipSecond = true;

			return searchEntryPoint.executeSearch(query, options)
					.flatMapObservable(SearchResultPageResource::getResults).map(SearchResultResource::getProperties);
		}

	}
}
