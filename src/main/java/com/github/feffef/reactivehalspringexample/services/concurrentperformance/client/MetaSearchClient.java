package com.github.feffef.reactivehalspringexample.services.concurrentperformance.client;

import com.github.feffef.reactivehalspringexample.api.search.SearchEntryPointResource;
import com.github.feffef.reactivehalspringexample.api.search.SearchOptions;
import com.github.feffef.reactivehalspringexample.api.search.SearchResult;
import com.github.feffef.reactivehalspringexample.api.search.SearchResultPageResource;
import com.github.feffef.reactivehalspringexample.api.search.SearchResultResource;

import io.reactivex.rxjava3.core.Observable;

public class MetaSearchClient {

	private final SearchEntryPointResource searchEntryPoint;

	public MetaSearchClient(SearchEntryPointResource searchEntryPoint) {
		this.searchEntryPoint = searchEntryPoint;
	}

	public Observable<SearchResult> getSearchResults(String query, int delayMs) {

		SearchOptions options = new SearchOptions();
		options.delayMs = delayMs;

		return searchEntryPoint.executeSearch(query, options).flatMapObservable(SearchResultPageResource::getResults)
				.map(SearchResultResource::getProperties);
	}
}
