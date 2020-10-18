package com.github.feffef.reactivehalspringexample.api.search;

import io.wcm.caravan.reha.api.annotations.HalApiInterface;
import io.wcm.caravan.reha.api.annotations.Related;
import io.wcm.caravan.reha.api.annotations.ResourceState;
import io.wcm.caravan.reha.api.relations.StandardRelations;

@HalApiInterface
public interface SearchResultResource {

	@ResourceState
	SearchResult getProperties();

	@Related(StandardRelations.EXTERNAL)
	ExternalHtmlResource getExternalLink();
}
