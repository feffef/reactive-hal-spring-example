package com.github.feffef.reactivehalspringexample.services.googlesearch.resource;

import java.util.concurrent.TimeUnit;

import com.github.feffef.reactivehalspringexample.api.search.SearchResultPageResource;
import com.github.feffef.reactivehalspringexample.api.search.SearchResultResource;
import com.github.feffef.reactivehalspringexample.services.googlesearch.context.GoogleSearchRequestContext;
import com.github.feffef.reactivehalspringexample.services.googlesearch.controller.GoogleSearchController;
import com.github.feffef.reactivehalspringexample.services.googlesearch.services.GoogleSearchService;

import io.reactivex.rxjava3.core.Maybe;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.Single;
import io.wcm.caravan.reha.api.resources.LinkableResource;
import io.wcm.caravan.hal.resource.Link;

public class GoogleSearchResultPageResource implements SearchResultPageResource, LinkableResource {

	private final static int RESULTS_PER_PAGE = 25;

	private final GoogleSearchRequestContext request;

	private final String query;
	private final Integer delayMs;
	private final Integer startIndex;

	private Single<GoogleSearchService.GoogleSearchResult> pageResult;

	public GoogleSearchResultPageResource(GoogleSearchRequestContext request, String query, Integer delayMs,
			Integer startIndex) {
		this.request = request;
		this.query = query;
		this.delayMs = delayMs;
		this.startIndex = startIndex;

		this.pageResult = request.getSearchService().getResults(query, startIndex, RESULTS_PER_PAGE).cache();
	}

	@Override
	public Observable<SearchResultResource> getResults() {

		return pageResult.flatMapObservable(r -> Observable.fromIterable(r.getResultsOnPage()))
				.delay(delayMs, TimeUnit.MILLISECONDS, false).map(GoogleSearchResultResource::new);
	}

	@Override
	public Maybe<SearchResultPageResource> getNextPage() {

		return pageResult.flatMapMaybe(r -> {
			int nextIndex = startIndex + RESULTS_PER_PAGE;
			if (r.getTotalNumResults() > nextIndex) {
				return linkToPage(nextIndex);
			}
			return Maybe.empty();
		});
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

	@Override
	public Link createLink() {
		return request.createLinkTo(GoogleSearchController.class,
				ctrl -> ctrl.getResultPage(query, delayMs, startIndex));
	}
}
