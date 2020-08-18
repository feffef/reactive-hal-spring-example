package com.github.feffef.reactivehalspringexample.services.metasearch;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.github.feffef.reactivehalspringexample.api.search.SearchEntryPointResource;
import com.github.feffef.reactivehalspringexample.api.search.SearchOptions;
import com.github.feffef.reactivehalspringexample.api.search.SearchResult;
import com.github.feffef.reactivehalspringexample.common.context.AbstractExampleRequestContext;
import com.github.feffef.reactivehalspringexample.common.services.LocalServiceRegistry;
import com.github.feffef.reactivehalspringexample.services.examplesearch.first.FirstSearchController;
import com.github.feffef.reactivehalspringexample.services.examplesearch.second.SecondSearchController;
import com.github.feffef.reactivehalspringexample.services.googlesearch.GoogleSearchController;
import com.github.feffef.reactivehalspringexample.services.metasearch.MetaSearchController.RequestContext;

import io.reactivex.rxjava3.core.Flowable;

@Component
public class MetaSearchResultProvider {

	@Autowired
	private LocalServiceRegistry serviceRegistry;

	private final MetaSearchResultMerger merger = new MetaSearchResultMerger();

	public Flowable<SearchResult> getMetaSearchResults(RequestContext request, String query, SearchOptions options) {

		SearchExecutor searchExecutor = new SearchExecutor(request, query, options);

		Flowable<SearchResult> firstResults = searchExecutor.getFirstResults();
		Flowable<SearchResult> secondResults = searchExecutor.getSecondResults();

		Flowable<SearchResult> mergedResults;
		if (options.skipSecond) {
			mergedResults = firstResults;
		} else if (options.skipFirst) {
			mergedResults = secondResults;
		} else {
			mergedResults = merger.merge(firstResults, secondResults);
		}

		return mergedResults;
	}

	private final class SearchExecutor {

		private final RequestContext request;
		private final String query;
		private final SearchOptions metaOptions;

		public SearchExecutor(RequestContext request, String query, SearchOptions metaOptions) {
			this.request = request;
			this.query = query;
			this.metaOptions = metaOptions;
		}

		Flowable<SearchResult> getFirstResults() {

			SearchOptions exampleOptions = new SearchOptions();
			exampleOptions.delayMs = metaOptions.delayMs;

			return executeSearchAndGetResultsAsFlowable(FirstSearchController.BASE_PATH, exampleOptions);
		}

		Flowable<SearchResult> getSecondResults() {

			SearchOptions exampleOptions = new SearchOptions();
			exampleOptions.delayMs = 500;

			return executeSearchAndGetResultsAsFlowable(SecondSearchController.BASE_PATH, exampleOptions);
		}

		Flowable<SearchResult> getGoogleResults() {

			SearchOptions googleOptions = new SearchOptions();

			return executeSearchAndGetResultsAsFlowable(GoogleSearchController.BASE_PATH, googleOptions);
		}

		private Flowable<SearchResult> executeSearchAndGetResultsAsFlowable(String basePath, SearchOptions options) {

			String entryPointUri = serviceRegistry.getServiceUrl(basePath);
			String immutableEntryPointUri = appendQueryTimestampParam(entryPointUri);

			SearchEntryPointResource searchEntryPoint = request.getEntryPoint(immutableEntryPointUri,
					SearchEntryPointResource.class);

			return searchEntryPoint.executeSearch(query, options).flatMapPublisher(merger::createAutoPagingFlowable);
		}

		private String appendQueryTimestampParam(String entryPointUri) {

			return entryPointUri + "?" + AbstractExampleRequestContext.QUERY_TIMESTAMP_PARAM + "="
					+ request.getQueryTimestamp().get();
		}
	}

}
