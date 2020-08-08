package com.github.feffef.reactivehalspringexample.services.metasearch.resource;

import com.github.feffef.reactivehalspringexample.api.search.SearchEntryPointResource;
import com.github.feffef.reactivehalspringexample.api.search.SearchOptions;
import com.github.feffef.reactivehalspringexample.api.search.SearchResultPageResource;
import com.github.feffef.reactivehalspringexample.services.metasearch.context.MetaSearchRequestContext;
import com.github.feffef.reactivehalspringexample.services.metasearch.controller.MetaSearchController;

import io.reactivex.rxjava3.core.Single;
import io.wcm.caravan.reha.api.resources.LinkableResource;
import io.wcm.caravan.hal.resource.Link;

public class MetaSearchEntryPointResource implements SearchEntryPointResource, LinkableResource {

	private final MetaSearchRequestContext request;

	public MetaSearchEntryPointResource(MetaSearchRequestContext request) {
		this.request = request;
	}

	@Override
	public Single<SearchResultPageResource> executeSearch(String query, SearchOptions options) {

		return Single.just(new MetaSearchResultPageResource(request, query, null, 0));
	}

	@Override
	public Link createLink() {

		return request.createLinkTo(MetaSearchController.class, ctrl -> ctrl.getEntryPoint());
	}

}
