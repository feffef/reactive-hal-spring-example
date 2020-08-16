package com.github.feffef.reactivehalspringexample.services.metasearch;

import com.github.feffef.reactivehalspringexample.api.search.SearchResult;
import com.github.feffef.reactivehalspringexample.api.search.SearchResultPageResource;
import com.github.feffef.reactivehalspringexample.api.search.SearchResultResource;

import io.reactivex.rxjava3.annotations.NonNull;
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

		// the Flowables created by #createAutoPagingFlowable show some undesirable behaviour once .zipWith
		// is being used on them. They will no longer only fetch as many upstream results as required, but will
		// eagerly fetch as many results as they can retrieve. This can be avoided by using #rebatchRequests
		Flowable<SearchResult> firstRebatched = firstResults.rebatchRequests(2);
		Flowable<SearchResult> secondRebatched = secondResults.rebatchRequests(2);

		// first use zip to find interleave as many results as are available in *both* flowables
		Flowable<SearchResult> zipped = firstRebatched.zipWith(secondRebatched, (r1, r2) -> {
			return Flowable.fromArray(r1, r2);
		}).flatMap(z -> z);

		// then add the remaining flowables (from the longer chain)
		// this is done using Flowable.defer to avoid the call on zipped.count() if the end of the zipped stream hasn't been reached yet
		return zipped.concatWith(Flowable.defer(() -> getRemaining(zipped, firstRebatched)))
				.concatWith(Flowable.defer(() -> getRemaining(zipped, secondRebatched)));
	}

	private Flowable<SearchResult> getRemaining(Flowable<SearchResult> zipped, Flowable<SearchResult> results) {

		return zipped.count().flatMapPublisher(numZipped -> results.skip(numZipped / 2));
	}

}
