package com.github.feffef.reactivehalspringexample.services.common.services;

import io.reactivex.rxjava3.core.Single;

public interface SearchResultProvider {

	Single<SearchProviderResult> getResults(String query, int startIndex, int numResults);

	int getMaxResultsPerPage();

}