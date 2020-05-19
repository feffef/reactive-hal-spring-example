package com.github.feffef.reactivehalspringexample.api.performance;

import io.reactivex.rxjava3.core.Single;
import io.wcm.caravan.hal.api.annotations.HalApiInterface;
import io.wcm.caravan.hal.api.annotations.RelatedResource;
import io.wcm.caravan.hal.api.annotations.TemplateVariable;

@HalApiInterface
public interface PerformanceEntryPointResource {

	@RelatedResource(relation = "performance:test")
	Single<PerformanceResultResource> executeRequest(@TemplateVariable("numRequests") Integer numRequests,
			@TemplateVariable("delayMs") Integer delayMs);
}
