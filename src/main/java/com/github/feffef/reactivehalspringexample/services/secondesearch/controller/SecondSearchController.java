package com.github.feffef.reactivehalspringexample.services.secondesearch.controller;

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
import com.github.feffef.reactivehalspringexample.api.search.SearchOptions;
import com.github.feffef.reactivehalspringexample.common.AbstractSpringRehaRequestContext;
import com.github.feffef.reactivehalspringexample.common.HalApiSupport;
import com.github.feffef.reactivehalspringexample.services.common.context.SearchProviderRequestContext;
import com.github.feffef.reactivehalspringexample.services.common.controller.SearchProviderController;
import com.github.feffef.reactivehalspringexample.services.common.resources.SearchEntryPointResourceImpl;
import com.github.feffef.reactivehalspringexample.services.common.resources.SearchResultPageResourceImpl;
import com.github.feffef.reactivehalspringexample.services.common.services.SearchResultProvider;
import com.github.feffef.reactivehalspringexample.services.secondsearch.services.SecondSearchResultProvider;

import io.wcm.caravan.reha.api.Reha;
import io.wcm.caravan.reha.api.resources.LinkableResource;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/search/second")
public class SecondSearchController implements SearchProviderController {

	@Autowired
	private HalApiSupport halSupport;

	@Autowired
	private SecondSearchResultProvider searchService;

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
		options.delayMs = ObjectUtils.defaultIfNull(delayMs, 0);
		
		return renderResource(
				request -> new SearchResultPageResourceImpl(request, query, options, startIndex));
	}

	Mono<ResponseEntity<JsonNode>> renderResource(
			Function<SearchProviderRequestContext, LinkableResource> resourceConstructor) {

		return halSupport.processRequest(RequestContext::new, resourceConstructor);
	}

	class RequestContext extends AbstractSpringRehaRequestContext<SearchProviderController>
			implements SearchProviderRequestContext {

		RequestContext(Reha reha) {
			super(reha, SecondSearchController.class);
		}

		@Override
		public SearchResultProvider getSearchResultProvider() {
			return searchService;
		}

	}
}
