package com.github.feffef.reactivehalspringexample.services.googlesearch.resource;

import java.util.concurrent.TimeUnit;

import com.github.feffef.reactivehalspringexample.api.search.SearchResultPageResource;
import com.github.feffef.reactivehalspringexample.api.search.SearchResultResource;
import com.github.feffef.reactivehalspringexample.services.googlesearch.context.GoogleSearchRequestContext;
import com.github.feffef.reactivehalspringexample.services.googlesearch.controller.GoogleSearchController;

import io.reactivex.rxjava3.core.Maybe;
import io.reactivex.rxjava3.core.Observable;
import io.wcm.caravan.hal.microservices.api.server.LinkableResource;
import io.wcm.caravan.hal.resource.Link;

public class GoogleSearchResultPageResource implements SearchResultPageResource, LinkableResource {

	private final static int RESULTS_PER_PAGE = 25;

	private final GoogleSearchRequestContext request;

	private final String query;
	private final Integer delayMs;
	private final Integer startIndex;

	public GoogleSearchResultPageResource(GoogleSearchRequestContext request, String query, Integer delayMs,
			Integer startIndex) {
		this.request = request;
		this.query = query;
		this.delayMs = delayMs;
		this.startIndex = startIndex;
	}

	@Override
	public Observable<SearchResultResource> getResults() {

		int numRemainingResults = Math.min(RESULTS_PER_PAGE, getTotalNumResults() - startIndex);

		return request.getSearchService().getResults(query, startIndex, numRemainingResults)
				.delay(delayMs, TimeUnit.MILLISECONDS, false).map(GoogleSearchResultResource::new);
	}

	@Override
	public Maybe<SearchResultPageResource> getNextPage() {

		int totalResults = getTotalNumResults();
		int nextIndex = startIndex + RESULTS_PER_PAGE;
		if (totalResults > nextIndex) {
			return linkToPage(nextIndex);
		}

		return Maybe.empty();
	}

	@Override
	public Maybe<SearchResultPageResource> getPreviousPage() {

		if (startIndex > 0) {
			int prevIndex = startIndex - RESULTS_PER_PAGE;
			return linkToPage(prevIndex);
		}

		return Maybe.empty();
	}

	private Maybe<SearchResultPageResource> linkToPage(int fromIndex) {
		return Maybe.just(new GoogleSearchResultPageResource(request, query, delayMs, fromIndex));
	}

	private int getTotalNumResults() {
		return Math.abs(query.hashCode()) % 100;
	}

	@Override
	public Link createLink() {
		return request.createLinkTo(GoogleSearchController.class,
				ctrl -> ctrl.getResultPage(query, delayMs, startIndex));
	}
}
