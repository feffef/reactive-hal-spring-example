package com.github.feffef.reactivehalspringexample.services.metasearch.results;

import com.github.feffef.reactivehalspringexample.api.search.SearchResult;
import com.github.feffef.reactivehalspringexample.api.search.SearchResultPageResource;
import com.github.feffef.reactivehalspringexample.api.search.SearchResultResource;

import io.reactivex.rxjava3.core.BackpressureStrategy;
import io.reactivex.rxjava3.core.Flowable;

public class MetaSearchResultMerger {

	public Flowable<SearchResult> createAutoPagingFlowable(SearchResultPageResource firstPage) {

		Flowable<SearchResult> resultsOnFirstPage = getResultsOnPage(firstPage);

		Flowable<SearchResult> resultsOnFollowingPages = getResultsOnFollowingPages(firstPage);

		return resultsOnFirstPage.concatWith(resultsOnFollowingPages);
	}

	private Flowable<SearchResult> getResultsOnPage(SearchResultPageResource page) {
		return page.getResults().map(SearchResultResource::getProperties).toFlowable(BackpressureStrategy.BUFFER);
	}

	private Flowable<SearchResult> getResultsOnFollowingPages(SearchResultPageResource currentPage) {

		return currentPage.getNextPage().flatMapPublisher(page -> {
			return getResultsOnPage(page).concatWith(getResultsOnFollowingPages(page));
		});
	}

	public Flowable<SearchResult> merge(Flowable<SearchResult> firstResults, Flowable<SearchResult> secondResults) {

		Flowable<Flowable<SearchResult>> zip = firstResults.rebatchRequests(2).zipWith(secondResults.rebatchRequests(2),
				(r1, r2) -> {
					return Flowable.fromArray(r1, r2);
				});
		return zip.flatMap(z -> z);
	}

}
