package com.github.feffef.reactivehalspringexample.api.index;

import java.util.List;

import io.wcm.caravan.reha.api.annotations.HalApiInterface;
import io.wcm.caravan.reha.api.annotations.RelatedResource;
import io.wcm.caravan.reha.api.resources.LinkableResource;

@HalApiInterface
public interface GlobalEntryPointResource extends LinkableResource {

	@RelatedResource(relation = "search:source")
	List<LinkableResource> getSearchServiceEntryPoints();

	@RelatedResource(relation = "search:meta")
	LinkableResource getMetaSeachEntryPoint();

	@RelatedResource(relation = "test:performance")
	List<LinkableResource> getPerformanceTestsEntryPoints();
}
