package com.github.feffef.reactivehalspringexample.services.metasearch;

import com.github.feffef.reactivehalspringexample.api.search.SearchEntryPointResource;
import com.github.feffef.reactivehalspringexample.api.search.SearchOptions;
import com.github.feffef.reactivehalspringexample.api.search.SearchResultPageResource;

import io.reactivex.rxjava3.core.Single;
import io.wcm.caravan.hal.resource.Link;
import io.wcm.caravan.reha.api.resources.LinkableResource;

public class MetaSearchEntryPointResource implements SearchEntryPointResource, LinkableResource {

	private final MetaSearchRequestContext request;

	public MetaSearchEntryPointResource(MetaSearchRequestContext request) {
		this.request = request;
	}

	@Override
	public Single<SearchResultPageResource> executeSearch(String query, SearchOptions options) {

		return Single.just(new MetaSearchResultPageResource(request, query, options, 0));
	}

	@Override
	public Link createLink() {

		return request.createLinkTo(ctrl -> ctrl.getEntryPoint())
				.setTitle("Entrypoint of the " + MetaSearchController.BASE_PATH + " service");
	}

}
