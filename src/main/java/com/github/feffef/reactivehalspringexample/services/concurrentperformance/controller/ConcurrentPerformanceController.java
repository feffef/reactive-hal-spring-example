package com.github.feffef.reactivehalspringexample.services.concurrentperformance.controller;

import java.util.function.Function;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.databind.JsonNode;
import com.github.feffef.reactivehalspringexample.api.search.SearchEntryPointResource;
import com.github.feffef.reactivehalspringexample.api.search.SearchResult;
import com.github.feffef.reactivehalspringexample.common.AbstractSpringRehaRequestContext;
import com.github.feffef.reactivehalspringexample.common.HalApiSupport;
import com.github.feffef.reactivehalspringexample.services.concurrentperformance.client.MetaSearchClient;
import com.github.feffef.reactivehalspringexample.services.concurrentperformance.context.ConcurrentPerformanceRequestContext;
import com.github.feffef.reactivehalspringexample.services.concurrentperformance.resource.ConcurrentPerformanceEntryPointResource;
import com.github.feffef.reactivehalspringexample.services.concurrentperformance.resource.ConcurrentPerformanceResultResource;

import io.reactivex.rxjava3.core.Observable;
import io.wcm.caravan.reha.api.Reha;
import io.wcm.caravan.reha.api.resources.LinkableResource;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/performance/concurrent")
public class ConcurrentPerformanceController {

	@Autowired
	private HalApiSupport halSupport;

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

		return halSupport.processRequest(RequestContext::new, resourceConstructor);
	}

	class RequestContext extends AbstractSpringRehaRequestContext<ConcurrentPerformanceController>
			implements ConcurrentPerformanceRequestContext {

		private final MetaSearchClient metaSearchClient;

		RequestContext(Reha reha) {
			super(reha, ConcurrentPerformanceController.class);

			metaSearchClient = new MetaSearchClient(
					getEntryPoint("http://localhost:8080/search/meta", SearchEntryPointResource.class));
		}

		@Override
		public Observable<SearchResult> getSearchResults(String query, int delayMs) {

			return metaSearchClient.getSearchResults(query, delayMs);
		}

	}
}
