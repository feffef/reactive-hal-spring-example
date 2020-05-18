package com.github.feffef.reactivehalspringexample.services.metasearch.resource;

import com.github.feffef.reactivehalspringexample.api.search.SearchOptions;
import com.github.feffef.reactivehalspringexample.api.search.SearchResult;
import com.github.feffef.reactivehalspringexample.api.search.SearchResultPageResource;
import com.github.feffef.reactivehalspringexample.api.search.SearchResultResource;
import com.github.feffef.reactivehalspringexample.services.metasearch.context.MetaSearchRequestContext;
import com.github.feffef.reactivehalspringexample.services.metasearch.controller.MetaSearchController;

import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.core.Maybe;
import io.reactivex.rxjava3.core.Observable;
import io.wcm.caravan.hal.microservices.api.server.LinkableResource;
import io.wcm.caravan.hal.resource.Link;

public class MetaSearchResultPageResource implements SearchResultPageResource, LinkableResource {

	private final static int RESULTS_PER_PAGE = 10;

	private final MetaSearchRequestContext request;

	private final String query;
	private final Integer delayMs;
	private final Integer startIndex;

	private Flowable<SearchResult> resultsOnPagePlusOne;

	public MetaSearchResultPageResource(MetaSearchRequestContext request, String query, Integer delayMs,
			Integer startIndex) {
		this.request = request;
		this.query = query;
		this.delayMs = delayMs;
		this.startIndex = startIndex;

		this.resultsOnPagePlusOne = getResultsOnPagePlusOneMore().cache();
	}

	private Flowable<SearchResult> getResultsOnPagePlusOneMore() {
		SearchOptions options = new SearchOptions();
		options.delayMs = delayMs;

		return request.getAllGoogleResults(query, options).skip(startIndex).take(RESULTS_PER_PAGE + 1);
	}

	@Override
	public Observable<SearchResultResource> getResults() {

		return resultsOnPagePlusOne.take(RESULTS_PER_PAGE).toObservable().map(MetaSearchResultResource::new);
	}

	@Override
	public Maybe<SearchResultPageResource> getNextPage() {

		int nextStartIndex = startIndex + RESULTS_PER_PAGE;

		Maybe<SearchResult> firstResultOnNextPage = resultsOnPagePlusOne.skip(RESULTS_PER_PAGE).firstElement();

		return firstResultOnNextPage.flatMap((firstResult) -> linkToPage(nextStartIndex));
	}

	@Override
	public Maybe<SearchResultPageResource> getPreviousPage() {

		int prevStartIndex = startIndex - RESULTS_PER_PAGE;

		return linkToPage(prevStartIndex);
	}

	private Maybe<SearchResultPageResource> linkToPage(int index) {
		if (index < 0) {
			return Maybe.empty();
		}
		return Maybe.just(new MetaSearchResultPageResource(request, query, delayMs, index));
	}

	@Override
	public Link createLink() {
		return request.createLinkTo(MetaSearchController.class, ctrl -> ctrl.getResultPage(query, delayMs, startIndex));
	}
}
