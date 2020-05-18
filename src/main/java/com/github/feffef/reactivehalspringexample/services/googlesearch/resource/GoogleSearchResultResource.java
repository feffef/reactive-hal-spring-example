package com.github.feffef.reactivehalspringexample.services.googlesearch.resource;

import com.github.feffef.reactivehalspringexample.api.search.SearchResult;
import com.github.feffef.reactivehalspringexample.api.search.SearchResultResource;

import io.wcm.caravan.hal.microservices.api.server.EmbeddableResource;

class GoogleSearchResultResource implements SearchResultResource, EmbeddableResource {

	private final SearchResult result;

	public GoogleSearchResultResource(SearchResult result) {
		this.result = result;
	}

	@Override
	public SearchResult getProperties() {
		return result;
	}

	@Override
	public boolean isEmbedded() {
		return true;
	}
}