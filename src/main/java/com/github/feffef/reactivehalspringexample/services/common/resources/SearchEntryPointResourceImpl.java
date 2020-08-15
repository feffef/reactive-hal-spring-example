package com.github.feffef.reactivehalspringexample.services.common.resources;

import com.github.feffef.reactivehalspringexample.api.search.SearchEntryPointResource;
import com.github.feffef.reactivehalspringexample.api.search.SearchOptions;
import com.github.feffef.reactivehalspringexample.api.search.SearchResultPageResource;
import com.github.feffef.reactivehalspringexample.services.common.context.SearchProviderRequestContext;

import io.reactivex.rxjava3.core.Single;
import io.wcm.caravan.hal.resource.Link;
import io.wcm.caravan.reha.api.resources.LinkableResource;

public class SearchEntryPointResourceImpl implements SearchEntryPointResource, LinkableResource {

	private final SearchProviderRequestContext request;

	public SearchEntryPointResourceImpl(SearchProviderRequestContext request) {
		this.request = request;
	}

	@Override
	public Single<SearchResultPageResource> executeSearch(String query, SearchOptions options) {

		SearchOptions defaultOptions = request.getSearchResultProvider().getDefaultOptions();
		
		return Single.just(new SearchResultPageResourceImpl(request, query, defaultOptions, 0));
	}

	@Override
	public Link createLink() {
		return request.createLinkTo(ctrl -> ctrl.getEntryPoint());
	}

}
