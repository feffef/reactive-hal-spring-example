package com.github.feffef.reactivehalspringexample.services.common.resources;

import java.util.concurrent.TimeUnit;

import com.github.feffef.reactivehalspringexample.api.search.SearchResultPageResource;
import com.github.feffef.reactivehalspringexample.api.search.SearchResultResource;
import com.github.feffef.reactivehalspringexample.services.common.context.SearchProviderRequestContext;
import com.github.feffef.reactivehalspringexample.services.common.services.SearchProviderResult;

import io.reactivex.rxjava3.core.Maybe;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.Single;
import io.wcm.caravan.hal.resource.Link;
import io.wcm.caravan.reha.api.resources.LinkableResource;

public class SearchResultPageResourceImpl implements SearchResultPageResource, LinkableResource {

	private final SearchProviderRequestContext request;

	private final String query;
	private final Integer delayMs;
	private final Integer startIndex;

	private final int maxResultsPerPage;

	private final Single<SearchProviderResult> pageResult;

	public SearchResultPageResourceImpl(SearchProviderRequestContext request, String query, Integer delayMs,
			Integer startIndex) {
		this.request = request;
		this.query = query;
		this.delayMs = delayMs;
		this.startIndex = startIndex;

		this.maxResultsPerPage = request.getSearchResultProvider().getMaxResultsPerPage();

		this.pageResult = request.getSearchResultProvider().getResults(query, startIndex, maxResultsPerPage).cache();
	}

	@Override
	public Observable<SearchResultResource> getResults() {

		return pageResult.flatMapObservable(r -> Observable.fromIterable(r.getResultsOnPage()))
				.delay(delayMs, TimeUnit.MILLISECONDS, false).map(SearchResultResourceImpl::new);
	}

	@Override
	public Maybe<SearchResultPageResource> getNextPage() {

		return pageResult.flatMapMaybe(r -> {
			int nextIndex = startIndex + maxResultsPerPage;
			if (r.getTotalNumResults() > nextIndex) {
				return linkToPage(nextIndex);
			}
			return Maybe.empty();
		});
	}

	@Override
	public Maybe<SearchResultPageResource> getPreviousPage() {

		if (startIndex > 0) {
			int prevIndex = startIndex - maxResultsPerPage;
			return linkToPage(prevIndex);
		}

		return Maybe.empty();
	}

	private Maybe<SearchResultPageResource> linkToPage(int fromIndex) {
		return Maybe.just(new SearchResultPageResourceImpl(request, query, delayMs, fromIndex));
	}

	@Override
	public Link createLink() {
		return request.createLinkTo(ctrl -> ctrl.getResultPage(query, delayMs, startIndex));
	}
}
