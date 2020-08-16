package com.github.feffef.reactivehalspringexample.common.resources;

import com.github.feffef.reactivehalspringexample.api.search.SearchResult;
import com.github.feffef.reactivehalspringexample.api.search.SearchResultResource;

import io.wcm.caravan.reha.api.resources.EmbeddableResource;

public class SearchResultResourceImpl implements SearchResultResource, EmbeddableResource {

	private final SearchResult result;

	public SearchResultResourceImpl(SearchResult result) {
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