package com.github.feffef.reactivehalspringexample.services.metasearch.resource;

import com.github.feffef.reactivehalspringexample.api.search.SearchOptions;
import com.github.feffef.reactivehalspringexample.api.search.SearchResultPageResource;
import com.github.feffef.reactivehalspringexample.api.search.SearchResultResource;
import com.github.feffef.reactivehalspringexample.services.metasearch.context.MetaSearchRequestContext;
import com.github.feffef.reactivehalspringexample.services.metasearch.controller.MetaSearchController;

import io.reactivex.rxjava3.core.Maybe;
import io.reactivex.rxjava3.core.Observable;
import io.wcm.caravan.hal.microservices.api.server.LinkableResource;
import io.wcm.caravan.hal.resource.Link;

public class MetaSearchResultPageResource implements SearchResultPageResource, LinkableResource {

	private final MetaSearchRequestContext request;

	private final String query;
	private final Integer delayMs;
	private final Integer startIndex;

	public MetaSearchResultPageResource(MetaSearchRequestContext request, String query, Integer delayMs,
			Integer startIndex) {
		this.request = request;
		this.query = query;
		this.delayMs = delayMs;
		this.startIndex = startIndex;
	}

	@Override
	public Observable<SearchResultResource> getResults() {

		SearchOptions options = new SearchOptions();
		options.delayMs = delayMs;

		return request.getGoogleSearchEntryPoint().executeSearch(query, options)
				.flatMapObservable(SearchResultPageResource::getResults).map(SearchResultResource::getProperties)
				.map(MetaSearchResultResource::new);
	}

	@Override
	public Maybe<SearchResultPageResource> getNextPage() {

		return Maybe.empty();
	}

	@Override
	public Maybe<SearchResultPageResource> getPreviousPage() {

		return Maybe.empty();
	}

	@Override
	public Link createLink() {
		return request.createLinkTo(MetaSearchController.class, ctrl -> ctrl.getResultPage(query, delayMs, startIndex));
	}
}
