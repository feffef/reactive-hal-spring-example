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
import com.github.feffef.reactivehalspringexample.api.search.SearchOptions;
import com.github.feffef.reactivehalspringexample.api.search.SearchResult;
import com.github.feffef.reactivehalspringexample.services.common.context.AbstractExampleRequestContext;
import com.github.feffef.reactivehalspringexample.services.metasearch.context.MetaSearchRequestContext;
import com.github.feffef.reactivehalspringexample.services.metasearch.resource.MetaSearchEntryPointResource;
import com.github.feffef.reactivehalspringexample.services.metasearch.resource.MetaSearchResultPageResource;
import com.github.feffef.reactivehalspringexample.services.metasearch.results.MetaSearchResultProvider;

import io.reactivex.rxjava3.core.Flowable;
import io.wcm.caravan.reha.api.resources.LinkableResource;
import io.wcm.caravan.reha.spring.api.SpringReactorReha;
import io.wcm.caravan.reha.spring.api.SpringRehaAsyncRequestProcessor;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/search/meta")
public class MetaSearchController {

	@Autowired
	private SpringRehaAsyncRequestProcessor requestProcessor;

	@GetMapping()
	public Mono<ResponseEntity<JsonNode>> getEntryPoint() {

		return renderResource(request -> new MetaSearchEntryPointResource(request));
	}

	@GetMapping("/results")
	public Mono<ResponseEntity<JsonNode>> getResultPage(@RequestParam String query,
			@RequestParam(required = false) Integer delayMs, @RequestParam(required = false) Boolean skipFirst,
			@RequestParam(required = false) Boolean skipSecond, @RequestParam Integer startIndex) {

		SearchOptions options = new SearchOptions();
		options.delayMs = defaultIfNull(delayMs, 0);
		options.skipFirst = defaultIfNull(skipFirst, false);
		options.skipSecond = defaultIfNull(skipSecond, false);

		return renderResource(request -> new MetaSearchResultPageResource(request, query, options, startIndex));
	}

	Mono<ResponseEntity<JsonNode>> renderResource(
			Function<MetaSearchRequestContext, LinkableResource> resourceConstructor) {

		return requestProcessor.processRequest(RequestContext::new, resourceConstructor);
	}

	class RequestContext extends AbstractExampleRequestContext<MetaSearchController>
			implements MetaSearchRequestContext {

		private final MetaSearchResultProvider provider;

		RequestContext(SpringReactorReha reha) {
			super(reha, MetaSearchController.class);

			this.provider = new MetaSearchResultProvider(reha);
		}

		@Override
		public Flowable<SearchResult> fetchAndMergeResults(String query, SearchOptions options) {
			return provider.getMetaSearchResults(query, options);
		}
	}
}
