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
import com.github.feffef.reactivehalspringexample.services.common.context.AbstractExampleRequestContext;
import com.github.feffef.reactivehalspringexample.services.examplesearch.first.FirstSearchController;
import com.github.feffef.reactivehalspringexample.services.examplesearch.second.SecondSearchController;
import com.github.feffef.reactivehalspringexample.services.googlesearch.controller.GoogleSearchController;
import com.github.feffef.reactivehalspringexample.services.metasearch.context.MetaSearchRequestContext;
import com.github.feffef.reactivehalspringexample.services.metasearch.resource.MetaSearchEntryPointResource;
import com.github.feffef.reactivehalspringexample.services.metasearch.resource.MetaSearchResultPageResource;
import com.github.feffef.reactivehalspringexample.services.metasearch.services.MetaSearchResultMerger;

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

	@Autowired
	private MetaSearchResultMerger merger;

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

		RequestContext(SpringReactorReha reha) {
			super(reha, MetaSearchController.class);
		}

		private SearchEntryPointResource getSearchEntryPoint(String basePath) {

			String baseUri = "http://localhost:8080";

			String entryPointUri = baseUri + basePath;

			return getEntryPoint(entryPointUri, SearchEntryPointResource.class);
		}

		private Flowable<SearchResult> executeSearchAndGetResultsAsFlowable(SearchEntryPointResource searchEntryPoint,
				String query, SearchOptions options) {

			return searchEntryPoint.executeSearch(query, options).flatMapPublisher(merger::createAutoPagingFlowable);
		}

		@Override
		public Flowable<SearchResult> getResultsFromFirst(String query, SearchOptions metaOptions) {

			SearchOptions exampleOptions = new SearchOptions();
			exampleOptions.delayMs = metaOptions.delayMs;

			SearchEntryPointResource exampleEntryPoint = getSearchEntryPoint(FirstSearchController.BASE_PATH);

			return executeSearchAndGetResultsAsFlowable(exampleEntryPoint, query, exampleOptions);
		}

		@Override
		public Flowable<SearchResult> getResultsFromSecond(String query, SearchOptions metaOptions) {

			SearchOptions exampleOptions = new SearchOptions();
			exampleOptions.delayMs = 500;

			SearchEntryPointResource exampleEntryPoint = getSearchEntryPoint(SecondSearchController.BASE_PATH);

			return executeSearchAndGetResultsAsFlowable(exampleEntryPoint, query, exampleOptions);
		}

		@Override
		public Flowable<SearchResult> getGoogleResults(String query, SearchOptions metaOptions) {

			SearchOptions googleOptions = new SearchOptions();

			SearchEntryPointResource exampleEntryPoint = getSearchEntryPoint(GoogleSearchController.BASE_PATH);

			return executeSearchAndGetResultsAsFlowable(exampleEntryPoint, query, googleOptions);
		}

		@Override
		public Flowable<SearchResult> merge(Flowable<SearchResult> exampleResults,
				Flowable<SearchResult> googleResults) {

			Flowable<Flowable<SearchResult>> zip = exampleResults.rebatchRequests(2)
					.zipWith(googleResults.rebatchRequests(2), (r1, r2) -> {
						return Flowable.fromArray(r1, r2);
					});
			return zip.flatMap(z -> z);
		}
	}
}
