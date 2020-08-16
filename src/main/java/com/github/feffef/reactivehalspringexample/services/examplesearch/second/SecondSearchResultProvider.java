package com.github.feffef.reactivehalspringexample.services.examplesearch.second;

import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.springframework.stereotype.Service;

import com.github.feffef.reactivehalspringexample.api.search.SearchOptions;
import com.github.feffef.reactivehalspringexample.api.search.SearchResult;
import com.github.feffef.reactivehalspringexample.common.services.SearchProviderResult;
import com.github.feffef.reactivehalspringexample.common.services.SearchResultProvider;

import io.reactivex.rxjava3.annotations.NonNull;
import io.reactivex.rxjava3.core.Single;
import io.wcm.caravan.reha.api.exceptions.HalApiDeveloperException;

@Service
public class SecondSearchResultProvider implements SearchResultProvider {

	public static final int MAX_RESULTS_PER_PAGE = 10;

	@Override
	public String getName() {
		return "second example search result provider";
	}

	@Override
	public Single<SearchProviderResult> getResults(String query, int startIndex, SearchOptions options) {

		if (query == null) {
			return Single.error(new HalApiDeveloperException("null query was given"));
		}

		int totalNumResults = getTotalNumResults(query);
		int endIndex = Math.min(totalNumResults, startIndex + MAX_RESULTS_PER_PAGE);

		List<SearchResult> results = IntStream.range(startIndex, endIndex).mapToObj(this::createResult)
				.collect(Collectors.toList());

		@NonNull
		Single<@NonNull SearchProviderResult> rxResults = Single.just(new SearchProviderResult() {

			@Override
			public int getTotalNumResults() {
				return totalNumResults;
			}

			@Override
			public List<SearchResult> getResultsOnPage() {
				return results;
			}

		});

		if (options.delayMs > 0) {
			rxResults = rxResults.delay(options.delayMs, TimeUnit.MILLISECONDS);
		}

		return rxResults;
	}

	SearchResult createResult(int index) {
		SearchResult result = new SearchResult();

		result.title = "Second Search Result #" + index;
		result.url = "https://www.example.com/result/" + index;

		return result;
	}

	protected int getTotalNumResults(String query) {
		return Math.abs(query.hashCode() * query.hashCode()) % 100;
	}

	@Override
	public int getMaxResultsPerPage() {
		return MAX_RESULTS_PER_PAGE;
	}

	@Override
	public SearchOptions getDefaultOptions() {

		return new SearchOptions();
	}

}
