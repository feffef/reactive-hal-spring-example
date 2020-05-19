package com.github.feffef.reactivehalspringexample.services.metasearch.controller;

import static org.apache.commons.lang3.ObjectUtils.defaultIfNull;

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
import com.github.feffef.reactivehalspringexample.common.AbstractHalServiceRequestContext;
import com.github.feffef.reactivehalspringexample.common.HalApiSupport;
import com.github.feffef.reactivehalspringexample.services.metasearch.context.MetaSearchRequestContext;
import com.github.feffef.reactivehalspringexample.services.metasearch.resource.MetaSearchEntryPointResource;
import com.github.feffef.reactivehalspringexample.services.metasearch.resource.MetaSearchResultPageResource;
import com.github.feffef.reactivehalspringexample.services.metasearch.services.MetaSearchResultMerger;

import io.reactivex.rxjava3.core.Flowable;
import io.wcm.caravan.hal.microservices.api.HalApiFacade;
import io.wcm.caravan.hal.microservices.api.server.LinkableResource;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/search/meta")
public class MetaSearchController {

	@Autowired
	private HalApiSupport halSupport;

	@Autowired
	private MetaSearchResultMerger merger;

	@GetMapping()
	public Mono<ResponseEntity<JsonNode>> getEntryPoint() {

		return renderResource(request -> new MetaSearchEntryPointResource(request));
	}

	@GetMapping("/results")
	public Mono<ResponseEntity<JsonNode>> getResultPage(@RequestParam("query") String query,
			@RequestParam("delayMs") Integer delayMs, @RequestParam("startIndex") Integer startIndex) {

		return renderResource(
				request -> new MetaSearchResultPageResource(request, query, defaultIfNull(delayMs, 0), startIndex));
	}

	Mono<ResponseEntity<JsonNode>> renderResource(
			Function<MetaSearchRequestContext, LinkableResource> resourceConstructor) {

		return halSupport.processRequest(RequestContext::new, resourceConstructor);
	}

	class RequestContext extends AbstractHalServiceRequestContext implements MetaSearchRequestContext {

		RequestContext(HalApiFacade halApi) {
			super(halApi);
		}

		private SearchEntryPointResource getGoogleSearchEntryPoint() {
			return getEntryPoint("http://localhost:8080/search/google", SearchEntryPointResource.class);
		}

		@Override
		public Flowable<SearchResult> getAllGoogleResults(String query, SearchOptions options) {

			return getGoogleSearchEntryPoint().executeSearch(query, options).flatMapPublisher(merger::getAllResults);
		}
	}
}
