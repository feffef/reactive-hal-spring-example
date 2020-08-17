package com.github.feffef.reactivehalspringexample.api.search;

import io.wcm.caravan.reha.api.annotations.HalApiInterface;
import io.wcm.caravan.reha.api.annotations.RelatedResource;
import io.wcm.caravan.reha.api.annotations.ResourceState;
import io.wcm.caravan.reha.api.relations.StandardRelations;

@HalApiInterface
public interface SearchResultResource {

	@ResourceState
	SearchResult getProperties();

	@RelatedResource(relation = StandardRelations.EXTERNAL)
	ExternalHtmlResource getExternalLink();
}
