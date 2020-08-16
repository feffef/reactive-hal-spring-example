package com.github.feffef.reactivehalspringexample.common.resources;

import com.github.feffef.reactivehalspringexample.api.search.SearchOptions;
import com.github.feffef.reactivehalspringexample.api.search.SearchResultPageResource;
import com.github.feffef.reactivehalspringexample.api.search.SearchResultResource;
import com.github.feffef.reactivehalspringexample.common.context.SearchProviderRequestContext;
import com.github.feffef.reactivehalspringexample.common.services.SearchProviderResult;
import com.github.feffef.reactivehalspringexample.common.services.SearchResultProvider;

import io.reactivex.rxjava3.core.Maybe;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.Single;
import io.wcm.caravan.hal.resource.Link;
import io.wcm.caravan.reha.api.resources.LinkableResource;

public class SearchResultPageResourceImpl implements SearchResultPageResource, LinkableResource {

	private final SearchProviderRequestContext request;

	private final String query;
	private final SearchOptions options;
	private final Integer startIndex;

	private final int maxResultsPerPage;

	private final Single<SearchProviderResult> pageResult;

	public SearchResultPageResourceImpl(SearchProviderRequestContext request, String query, SearchOptions options,
			Integer startIndex) {
		this.request = request;
		this.query = query;
		this.options = options;
		this.startIndex = startIndex;

		this.maxResultsPerPage = request.getSearchResultProvider().getMaxResultsPerPage();

		this.pageResult = request.getSearchResultProvider().getResults(query, startIndex, options).cache();
	}

	@Override
	public Observable<SearchResultResource> getResults() {

		return pageResult.flatMapObservable(r -> Observable.fromIterable(r.getResultsOnPage()))
				.map(SearchResultResourceImpl::new);
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
		return Maybe.just(new SearchResultPageResourceImpl(request, query, options, fromIndex));
	}

	@Override
	public Link createLink() {

		return request.createLinkTo(ctrl -> ctrl.getResultPage(query, options.delayMs, startIndex))
				.setTitle(getLinkTitle());
	}

	private String getLinkTitle() {

		SearchResultProvider provider = request.getSearchResultProvider();

		if (query == null) {
			return "Execute a search query with the " + provider.getName();
		}

		int resultsPerPage = provider.getMaxResultsPerPage();
		int pageIndex = startIndex / resultsPerPage;
		int lastIndex = startIndex + resultsPerPage - 1;

		return "Page " + pageIndex + " with results " + startIndex + "-" + lastIndex + " from the " + provider.getName()
				+ " service";
	}
}
