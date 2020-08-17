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
	private final String memento;

	public SearchEntryPointResourceImpl(SearchProviderRequestContext request, String memento) {
		this.request = request;
		this.memento = memento;
	}

	@Override
	public Single<SearchResultPageResource> executeSearch(String query, SearchOptions options) {

		SearchOptions defaultOptions = request.getSearchResultProvider().getDefaultOptions();

		return Single.just(new SearchResultPageResourceImpl(request, query, defaultOptions, 0));
	}

	@Override
	public Maybe<SearchEntryPointResource> getImmutableEntryPoint(String memento) {

		// don't render a link if the resource was already laoded with a memento
		// parameter
		if (request.getMemento().isPresent()) {
			return Maybe.empty();
		}

		// render a link template containing the memento variable (by leaving it to
		// null)
		return Maybe.just(new SearchEntryPointResourceImpl(request, null));
	}

	@Override
	public Link createLink() {

		Link link = request.createLinkTo(ctrl -> ctrl.getEntryPoint(memento));

		link.setTitle("Entry point of the " + request.getSearchResultProvider().getName() + " service");

		return link;
	}

}
