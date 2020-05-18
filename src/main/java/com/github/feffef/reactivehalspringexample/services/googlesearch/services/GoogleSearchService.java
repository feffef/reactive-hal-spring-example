package com.github.feffef.reactivehalspringexample.services.googlesearch.services;

import org.springframework.stereotype.Service;

import com.github.feffef.reactivehalspringexample.api.search.SearchResult;

import io.reactivex.rxjava3.core.Observable;

@Service
public class GoogleSearchService {

	public Observable<SearchResult> getResults(String query, int startIndex, int numResults) {

		return Observable.range(startIndex, numResults).map(this::createResult);
	}

	SearchResult createResult(int index) {
		SearchResult result = new SearchResult();

		result.title = "Google Search Result #" + index;
		result.url = "https://www.example.com/result/" + index;

		return result;
	}
}
