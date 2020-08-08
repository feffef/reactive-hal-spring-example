package com.github.feffef.reactivehalspringexample.services.googlesearch.controller;

import static org.apache.commons.lang3.ObjectUtils.defaultIfNull;

import java.util.function.Function;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.databind.JsonNode;
import com.github.feffef.reactivehalspringexample.common.AbstractHalServiceRequestContext;
import com.github.feffef.reactivehalspringexample.common.HalApiSupport;
import com.github.feffef.reactivehalspringexample.services.googlesearch.context.GoogleSearchRequestContext;
import com.github.feffef.reactivehalspringexample.services.googlesearch.resource.GoogleSearchEntryPointResource;
import com.github.feffef.reactivehalspringexample.services.googlesearch.resource.GoogleSearchResultPageResource;
import com.github.feffef.reactivehalspringexample.services.googlesearch.services.GoogleSearchService;

import io.wcm.caravan.reha.api.Reha;
import io.wcm.caravan.reha.api.resources.LinkableResource;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/search/google")
public class GoogleSearchController {

	@Autowired
	private HalApiSupport halSupport;

	@Autowired
	private GoogleSearchService searchService;

	@GetMapping()
	public Mono<ResponseEntity<JsonNode>> getEntryPoint() {

		return renderResource(request -> new GoogleSearchEntryPointResource(request));
	}

	@GetMapping("/results")
	public Mono<ResponseEntity<JsonNode>> getResultPage(@RequestParam("query") String query,
			@RequestParam("delayMs") Integer delayMs, @RequestParam("startIndex") Integer startIndex) {

		return renderResource(
				request -> new GoogleSearchResultPageResource(request, query, defaultIfNull(delayMs, 0), startIndex));
	}

	Mono<ResponseEntity<JsonNode>> renderResource(
			Function<GoogleSearchRequestContext, LinkableResource> resourceConstructor) {

		return halSupport.processRequest(RequestContext::new, resourceConstructor);
	}

	class RequestContext extends AbstractHalServiceRequestContext implements GoogleSearchRequestContext {

		RequestContext(Reha reha) {
			super(reha);
		}

		@Override
		public GoogleSearchService getSearchService() {
			return searchService;
		}

	}
}
