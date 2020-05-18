package com.github.feffef.reactivehalspringexample.services.googlesearch.resource;

import com.github.feffef.reactivehalspringexample.api.search.SearchEntryPointResource;
import com.github.feffef.reactivehalspringexample.api.search.SearchOptions;
import com.github.feffef.reactivehalspringexample.api.search.SearchResultPageResource;
import com.github.feffef.reactivehalspringexample.services.googlesearch.context.GoogleSearchRequestContext;

import io.reactivex.rxjava3.core.Single;
import io.wcm.caravan.hal.microservices.api.server.LinkableResource;
import io.wcm.caravan.hal.resource.Link;

public class GoogleSearchEntryPointResource implements SearchEntryPointResource, LinkableResource {

	private final GoogleSearchRequestContext request;

	public GoogleSearchEntryPointResource(GoogleSearchRequestContext request) {
		this.request = request;
	}

	@Override
	public Single<SearchResultPageResource> executeSearch(String query, SearchOptions options) {

		return Single.just(new GoogleSearchResultPageResource(request, query, null, 0));
	}

	@Override
	public Link createLink() {
		return request.createLinkTo(ctrl -> ctrl.getEntryPoint());
	}

}
