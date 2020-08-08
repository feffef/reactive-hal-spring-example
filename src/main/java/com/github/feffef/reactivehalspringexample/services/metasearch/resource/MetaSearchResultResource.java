package com.github.feffef.reactivehalspringexample.services.metasearch.resource;

import com.github.feffef.reactivehalspringexample.api.search.SearchResult;
import com.github.feffef.reactivehalspringexample.api.search.SearchResultResource;

import io.wcm.caravan.reha.api.resources.EmbeddableResource;

class MetaSearchResultResource implements SearchResultResource, EmbeddableResource {

	private final SearchResult result;

	public MetaSearchResultResource(SearchResult result) {
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