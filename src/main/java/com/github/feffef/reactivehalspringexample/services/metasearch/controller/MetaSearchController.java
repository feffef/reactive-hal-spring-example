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
import com.github.feffef.reactivehalspringexample.common.AbstractSpringRehaRequestContext;
import com.github.feffef.reactivehalspringexample.common.HalApiSupport;
import com.github.feffef.reactivehalspringexample.services.metasearch.context.MetaSearchRequestContext;
import com.github.feffef.reactivehalspringexample.services.metasearch.resource.MetaSearchEntryPointResource;
import com.github.feffef.reactivehalspringexample.services.metasearch.resource.MetaSearchResultPageResource;
import com.github.feffef.reactivehalspringexample.services.metasearch.services.MetaSearchResultMerger;

import io.reactivex.rxjava3.core.Flowable;
import io.wcm.caravan.reha.api.Reha;
import io.wcm.caravan.reha.api.resources.LinkableResource;
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

		private SearchEntryPointResource getSearchEntryPoint(String entryPointUri) {

			return getEntryPoint(entryPointUri, SearchEntryPointResource.class);
		}

		private Flowable<SearchResult> executeSearchAndGetResultsAsFlowable(SearchEntryPointResource searchEntryPoint,
				String query, SearchOptions options) {

			return searchEntryPoint.executeSearch(query, options).flatMapPublisher(merger::createAutoPagingFlowable);
		}

		@Override
		public Flowable<SearchResult> getAllExampleResults(String query, SearchOptions metaOptions) {

			SearchOptions exampleOptions = new SearchOptions();
			exampleOptions.delayMs = metaOptions.delayMs;

			SearchEntryPointResource exampleEntryPoint = getSearchEntryPoint("http://localhost:8080/search/example");

			return executeSearchAndGetResultsAsFlowable(exampleEntryPoint, query, exampleOptions);
		}

		@Override
		public Flowable<SearchResult> getGoogleResults(String query, SearchOptions metaOptions) {

			SearchOptions googleOptions = new SearchOptions();

			SearchEntryPointResource exampleEntryPoint = getSearchEntryPoint("http://localhost:8080/search/google");

			return executeSearchAndGetResultsAsFlowable(exampleEntryPoint, query, googleOptions);
		}

		@Override
		public Flowable<SearchResult> merge(Flowable<SearchResult> exampleResults,
				Flowable<SearchResult> googleResults) {

			Flowable<Flowable<SearchResult>> zip = exampleResults.zipWith(googleResults, (r1, r2) -> {
				return Flowable.fromArray(r1, r2);
			});
			return zip.flatMap(z -> z);
		}
	}
}
