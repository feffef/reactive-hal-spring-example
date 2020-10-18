package com.github.feffef.reactivehalspringexample.api.global;

import java.util.List;

import io.wcm.caravan.reha.api.annotations.HalApiInterface;
import io.wcm.caravan.reha.api.annotations.Related;
import io.wcm.caravan.reha.api.resources.LinkableResource;

@HalApiInterface
public interface GlobalEntryPointResource extends LinkableResource {

	@Related("search:source")
	List<LinkableResource> getSearchServiceEntryPoints();

	@Related("search:meta")
	LinkableResource getMetaSeachEntryPoint();

	@Related("test:performance")
	List<LinkableResource> getPerformanceTestsEntryPoints();
}
