package com.github.feffef.reactivehalspringexample.services.examplesearch.first;

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
import com.github.feffef.reactivehalspringexample.common.context.AbstractExampleRequestContext;
import com.github.feffef.reactivehalspringexample.common.context.SearchProviderRequestContext;
import com.github.feffef.reactivehalspringexample.common.controller.SearchProviderController;
import com.github.feffef.reactivehalspringexample.common.resources.SearchEntryPointResourceImpl;
import com.github.feffef.reactivehalspringexample.common.resources.SearchResultPageResourceImpl;
import com.github.feffef.reactivehalspringexample.common.services.SearchResultProvider;

import io.wcm.caravan.rhyme.api.resources.LinkableResource;
import io.wcm.caravan.rhyme.spring.api.SpringReactorRhyme;
import io.wcm.caravan.rhyme.spring.api.SpringRhymeAsyncRequestProcessor;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping(FirstSearchController.BASE_PATH)
public class FirstSearchController implements SearchProviderController {

	public final static String BASE_PATH = "/search/first";

	@Autowired
	private SpringRhymeAsyncRequestProcessor requestProcessor;

	@Autowired
	private FirstSearchResultProvider searchService;

	@Override
	@GetMapping()
	public Mono<ResponseEntity<JsonNode>> getEntryPoint(@RequestParam(required = false) String queryTimestamp) {

		return renderResource(request -> new SearchEntryPointResourceImpl(request, defaultIfNull(queryTimestamp, "")));
	}

	@Override
	@GetMapping("/results")
	public Mono<ResponseEntity<JsonNode>> getResultPage(
			@RequestParam("query") String query,
			@RequestParam(name = "delayMs", required = false) Integer delayMs,
			@RequestParam("startIndex") Integer startIndex) {

		SearchOptions options = new SearchOptions();
		options.delayMs = ObjectUtils.defaultIfNull(delayMs, 0);

		return renderResource(request -> new SearchResultPageResourceImpl(request, query, options, startIndex));
	}

	Mono<ResponseEntity<JsonNode>> renderResource(
			Function<SearchProviderRequestContext, LinkableResource> resourceConstructor) {

		return requestProcessor.processRequest(RequestContext::new, resourceConstructor);
	}

	class RequestContext extends AbstractExampleRequestContext<SearchProviderController>
			implements SearchProviderRequestContext {

		RequestContext(SpringReactorRhyme rhyme) {
			super(rhyme, FirstSearchController.class);
		}

		@Override
		public SearchResultProvider getSearchResultProvider() {
			return searchService;
		}

	}
}
