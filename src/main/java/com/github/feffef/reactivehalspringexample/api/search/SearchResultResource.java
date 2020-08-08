package com.github.feffef.reactivehalspringexample.api.search;

import io.wcm.caravan.reha.api.annotations.HalApiInterface;
import io.wcm.caravan.reha.api.annotations.ResourceState;

@HalApiInterface
public interface SearchResultResource {

	@ResourceState
	SearchResult getProperties();
}
