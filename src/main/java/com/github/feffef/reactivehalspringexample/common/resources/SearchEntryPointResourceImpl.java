package com.github.feffef.reactivehalspringexample.common.resources;

import com.github.feffef.reactivehalspringexample.api.search.SearchEntryPointResource;
import com.github.feffef.reactivehalspringexample.api.search.SearchOptions;
import com.github.feffef.reactivehalspringexample.api.search.SearchResultPageResource;
import com.github.feffef.reactivehalspringexample.common.context.SearchProviderRequestContext;

import io.reactivex.rxjava3.core.Maybe;
import io.reactivex.rxjava3.core.Single;
import io.wcm.caravan.hal.resource.Link;
import io.wcm.caravan.reha.api.resources.LinkableResource;

public class SearchEntryPointResourceImpl implements SearchEntryPointResource, LinkableResource {

	private final SearchProviderRequestContext request;
	private final String queryTimestamp;

	public SearchEntryPointResourceImpl(SearchProviderRequestContext request, String queryTimestamp) {
		this.request = request;
		this.queryTimestamp = queryTimestamp;
	}

	@Override
	public Single<SearchResultPageResource> executeSearch(String query, SearchOptions options) {

		SearchOptions defaultOptions = request.getSearchResultProvider().getDefaultOptions();

		return Single.just(new SearchResultPageResourceImpl(request, query, defaultOptions, 0));
	}

	@Override
	public Maybe<SearchEntryPointResource> getImmutableEntryPoint(String queryTimestamp) {

		// don't render a link if the resource was already loaded with a timestamp param
		if (request.getQueryTimestamp().isPresent()) {
			return Maybe.empty();
		}

		// otherwise render a link template containing the timestamp variable
		// (by leaving it to null)
		return Maybe.just(new SearchEntryPointResourceImpl(request, null));
	}

	@Override
	public Link createLink() {

		return request.createLinkTo(ctrl -> ctrl.getEntryPoint(queryTimestamp)).setTitle(getLinkTitle());
	}

	private String getLinkTitle() {

		if (queryTimestamp == null) {
			return "Load a version of this entry point that is immutable (due to fingerprinting in the URL)";
		}

		return "Entry point of the " + request.getSearchResultProvider().getName() + " service";
	}
}
