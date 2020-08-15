package com.github.feffef.reactivehalspringexample.services.examplesearch.services;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.springframework.stereotype.Service;

import com.github.feffef.reactivehalspringexample.api.search.SearchResult;
import com.github.feffef.reactivehalspringexample.services.common.services.SearchProviderResult;
import com.github.feffef.reactivehalspringexample.services.common.services.SearchResultProvider;

import io.reactivex.rxjava3.core.Single;
import io.wcm.caravan.reha.api.exceptions.HalApiDeveloperException;

@Service
public class ExampleSearchResultProvider implements SearchResultProvider {

	@Override
	public Single<SearchProviderResult> getResults(String query, int startIndex, int numResults) {

		if (query == null) {
			return Single.error(new HalApiDeveloperException("null query was given"));
		}

		int totalNumResults = getTotalNumResults(query);
		int endIndex = Math.min(totalNumResults, startIndex + numResults);

		List<SearchResult> results = IntStream.range(startIndex, endIndex).mapToObj(this::createResult)
				.collect(Collectors.toList());

		return Single.just(new SearchProviderResult() {

			@Override
			public int getTotalNumResults() {
				return totalNumResults;
			}

			@Override
			public List<SearchResult> getResultsOnPage() {
				return results;
			}

		});
	}

	SearchResult createResult(int index) {
		SearchResult result = new SearchResult();

		result.title = "Example Search Result #" + index;
		result.url = "https://www.example.com/result/" + index;

		return result;
	}

	@Override
	public int getTotalNumResults(String query) {
		return Math.abs(query.hashCode()) % 100;
	}

}
