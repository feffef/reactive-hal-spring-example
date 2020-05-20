package com.github.feffef.reactivehalspringexample.services.googlesearch.services;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.springframework.stereotype.Service;

import com.github.feffef.reactivehalspringexample.api.search.SearchResult;

import io.reactivex.rxjava3.core.Single;
import io.wcm.caravan.hal.microservices.api.client.HalApiDeveloperException;

@Service
public class GoogleSearchService {

	public Single<GoogleSearchResult> getResults(String query, int startIndex, int numResults) {

		if (query == null) {
			return Single.error(new HalApiDeveloperException("null query was given"));
		}

		int totalNumResults = getTotalNumResults(query);
		int endIndex = Math.min(totalNumResults, startIndex + numResults);

		List<SearchResult> results = IntStream.range(startIndex, endIndex).mapToObj(this::createResult)
				.collect(Collectors.toList());

		return Single.just(new GoogleSearchResult() {

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

		result.title = "Google Search Result #" + index;
		result.url = "https://www.example.com/result/" + index;

		return result;
	}

	public int getTotalNumResults(String query) {
		return Math.abs(query.hashCode()) % 100;
	}

	public interface GoogleSearchResult {

		int getTotalNumResults();

		List<SearchResult> getResultsOnPage();
	}

}
