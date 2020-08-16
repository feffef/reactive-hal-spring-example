package com.github.feffef.reactivehalspringexample.services.common.services;

import com.github.feffef.reactivehalspringexample.api.search.SearchOptions;

import io.reactivex.rxjava3.core.Single;

public interface SearchResultProvider {

	Single<SearchProviderResult> getResults(String query, int startIndex, SearchOptions options);

	int getMaxResultsPerPage();

	SearchOptions getDefaultOptions();

	String getName();

}