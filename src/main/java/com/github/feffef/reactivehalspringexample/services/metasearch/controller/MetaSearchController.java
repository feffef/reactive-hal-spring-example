package com.github.feffef.reactivehalspringexample.services.metasearch.controller;

import static org.apache.commons.lang3.ObjectUtils.defaultIfNull;

import java.util.function.Function;

import org.apache.commons.lang3.ObjectUtils;
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
import com.github.feffef.reactivehalspringexample.common.AbstractSpringRehaRequestContext;
import com.github.feffef.reactivehalspringexample.common.HalApiSupport;
import com.github.feffef.reactivehalspringexample.services.metasearch.context.MetaSearchRequestContext;
import com.github.feffef.reactivehalspringexample.services.metasearch.resource.MetaSearchEntryPointResource;
import com.github.feffef.reactivehalspringexample.services.metasearch.resource.MetaSearchResultPageResource;
import com.github.feffef.reactivehalspringexample.services.metasearch.services.MetaSearchResultMerger;

import io.reactivex.rxjava3.core.Flowable;
import io.wcm.caravan.reha.api.Reha;
import io.wcm.caravan.reha.api.resources.LinkableResource;
import jdk.nashorn.internal.objects.annotations.Optimistic;
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
	public Mono<ResponseEntity<JsonNode>> getResultPage(@RequestParam String query,
			@RequestParam(required = false) Integer delayMs, @RequestParam(required = false) Boolean skipExample,
			@RequestParam(required = false) Boolean skipGoogle, @RequestParam Integer startIndex) {

		SearchOptions options = new SearchOptions();
		options.delayMs = defaultIfNull(delayMs, 0);
		options.skipExample = defaultIfNull(skipExample, false);
		options.skipGoogle = defaultIfNull(skipGoogle, false);

		return renderResource(request -> new MetaSearchResultPageResource(request, query, options, startIndex));
	}

	Mono<ResponseEntity<JsonNode>> renderResource(
			Function<MetaSearchRequestContext, LinkableResource> resourceConstructor) {

		return halSupport.processRequest(RequestContext::new, resourceConstructor);
	}

	class RequestContext extends AbstractSpringRehaRequestContext<MetaSearchController>
			implements MetaSearchRequestContext {

		RequestContext(Reha reha) {
			super(reha, MetaSearchController.class);
		}

		private SearchEntryPointResource getExampleSearchEntryPoint() {
			return getEntryPoint("http://localhost:8080/search/example", SearchEntryPointResource.class);
		}

		@Override
		public Flowable<SearchResult> getAllExampleResults(String query, SearchOptions options) {

			SearchOptions exampleOptions = new SearchOptions();
			exampleOptions.delayMs = options.delayMs;

			return getExampleSearchEntryPoint().executeSearch(query, exampleOptions)
					.flatMapPublisher(merger::getAllResults);
		}
	}
}
