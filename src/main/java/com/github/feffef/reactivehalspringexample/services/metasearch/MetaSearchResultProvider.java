package com.github.feffef.reactivehalspringexample.services.metasearch;

import com.github.feffef.reactivehalspringexample.api.search.SearchEntryPointResource;
import com.github.feffef.reactivehalspringexample.api.search.SearchOptions;
import com.github.feffef.reactivehalspringexample.api.search.SearchResult;
import com.github.feffef.reactivehalspringexample.common.context.AbstractExampleRequestContext;
import com.github.feffef.reactivehalspringexample.services.examplesearch.first.FirstSearchController;
import com.github.feffef.reactivehalspringexample.services.examplesearch.second.SecondSearchController;
import com.github.feffef.reactivehalspringexample.services.googlesearch.GoogleSearchController;
import com.github.feffef.reactivehalspringexample.services.metasearch.MetaSearchController.RequestContext;

import io.reactivex.rxjava3.core.Flowable;

public class MetaSearchResultProvider {

	private final RequestContext request;

	private final MetaSearchResultMerger merger = new MetaSearchResultMerger();

	public MetaSearchResultProvider(RequestContext context) {
		this.request = context;
	}

	public Flowable<SearchResult> getMetaSearchResults(String query, SearchOptions options) {

		Flowable<SearchResult> firstResults = getFirstResults(query, options);
		Flowable<SearchResult> secondResults = getSecondResults(query, options);

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

	Flowable<SearchResult> getFirstResults(String query, SearchOptions metaOptions) {

		SearchOptions exampleOptions = new SearchOptions();
		exampleOptions.delayMs = metaOptions.delayMs;

		return executeSearchAndGetResultsAsFlowable(FirstSearchController.BASE_PATH, query, exampleOptions);
	}

	Flowable<SearchResult> getSecondResults(String query, SearchOptions metaOptions) {

		SearchOptions exampleOptions = new SearchOptions();
		exampleOptions.delayMs = 500;

		return executeSearchAndGetResultsAsFlowable(SecondSearchController.BASE_PATH, query, exampleOptions);
	}

	Flowable<SearchResult> getGoogleResults(String query, SearchOptions metaOptions) {

		SearchOptions googleOptions = new SearchOptions();

		return executeSearchAndGetResultsAsFlowable(GoogleSearchController.BASE_PATH, query, googleOptions);
	}

	private Flowable<SearchResult> executeSearchAndGetResultsAsFlowable(String basePath, String query,
			SearchOptions options) {

		String entryPointUri = "http://localhost:8080" + basePath;
		String immutableEntryPointuri = appendQueryTimestampParam(entryPointUri);

		SearchEntryPointResource searchEntryPoint = request.getEntryPoint(immutableEntryPointuri,
				SearchEntryPointResource.class);

		return searchEntryPoint.executeSearch(query, options).flatMapPublisher(merger::createAutoPagingFlowable);
	}

	private String appendQueryTimestampParam(String entryPointUri) {

		return entryPointUri + "?" + AbstractExampleRequestContext.QUERY_TIMESTAMP_PARAM + "="
				+ request.getQueryTimestamp().get();
	}
}
