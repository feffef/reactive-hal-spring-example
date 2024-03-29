package com.github.feffef.reactivehalspringexample.api.global;

import java.util.List;

import io.wcm.caravan.hal.resource.Link;
import io.wcm.caravan.rhyme.api.annotations.HalApiInterface;
import io.wcm.caravan.rhyme.api.annotations.Related;
import io.wcm.caravan.rhyme.api.resources.LinkableResource;

@HalApiInterface
public interface GlobalEntryPointResource extends LinkableResource {

	@Related("search:source")
	List<Link> getSearchServiceEntryPoints();

	@Related("search:meta")
	Link getMetaSeachEntryPoint();

	@Related("test:performance")
	List<Link> getPerformanceTestsEntryPoints();
}
