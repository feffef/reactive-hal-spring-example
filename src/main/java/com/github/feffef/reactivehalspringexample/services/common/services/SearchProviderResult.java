package com.github.feffef.reactivehalspringexample.services.common.services;

import java.util.List;

import com.github.feffef.reactivehalspringexample.api.search.SearchResult;

public interface SearchProviderResult {

	int getTotalNumResults();

	List<SearchResult> getResultsOnPage();
}