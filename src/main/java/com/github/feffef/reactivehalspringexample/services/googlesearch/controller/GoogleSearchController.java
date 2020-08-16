package com.github.feffef.reactivehalspringexample.services.googlesearch.controller;

import java.util.function.Function;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.databind.JsonNode;
import com.github.feffef.reactivehalspringexample.api.search.SearchOptions;
import com.github.feffef.reactivehalspringexample.services.common.context.AbstractExampleRequestContext;
import com.github.feffef.reactivehalspringexample.services.common.context.SearchProviderRequestContext;
import com.github.feffef.reactivehalspringexample.services.common.controller.SearchProviderController;
import com.github.feffef.reactivehalspringexample.services.common.resources.SearchEntryPointResourceImpl;
import com.github.feffef.reactivehalspringexample.services.common.resources.SearchResultPageResourceImpl;
import com.github.feffef.reactivehalspringexample.services.common.services.SearchResultProvider;
import com.github.feffef.reactivehalspringexample.services.googlesearch.services.GoogleSearchResultProvider;

import io.wcm.caravan.reha.api.resources.LinkableResource;
import io.wcm.caravan.reha.spring.api.SpringReactorReha;
import io.wcm.caravan.reha.spring.api.SpringRehaAsyncRequestProcessor;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/search/google")
public class GoogleSearchController implements SearchProviderController {

	@Autowired
	private SpringRehaAsyncRequestProcessor requestProcessor;

	@Autowired
	private GoogleSearchResultProvider googleResultProvider;

	@Override
	@GetMapping()
	public Mono<ResponseEntity<JsonNode>> getEntryPoint() {

		return renderResource(request -> new SearchEntryPointResourceImpl(request));
	}

	@Override
	@GetMapping("/results")
	public Mono<ResponseEntity<JsonNode>> getResultPage(@RequestParam("query") String query,
			@RequestParam("delayMs") Integer delayMs, @RequestParam("startIndex") Integer startIndex) {

		SearchOptions options = new SearchOptions();
		options.delayMs = delayMs;

		return renderResource(request -> new SearchResultPageResourceImpl(request, query, options, startIndex));
	}

	Mono<ResponseEntity<JsonNode>> renderResource(
			Function<SearchProviderRequestContext, LinkableResource> resourceConstructor) {

		return requestProcessor.processRequest(RequestContext::new, resourceConstructor);
	}

	class RequestContext extends AbstractExampleRequestContext<SearchProviderController>
			implements SearchProviderRequestContext {

		RequestContext(SpringReactorReha reha) {
			super(reha, GoogleSearchController.class);
		}

		@Override
		public SearchResultProvider getSearchResultProvider() {
			return googleResultProvider;
		}

	}
}
