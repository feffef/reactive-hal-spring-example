package com.github.feffef.reactivehalspringexample.api.performance;

import io.reactivex.rxjava3.core.Single;
import io.wcm.caravan.reha.api.annotations.HalApiInterface;
import io.wcm.caravan.reha.api.annotations.Related;
import io.wcm.caravan.reha.api.annotations.TemplateVariable;

@HalApiInterface
public interface PerformanceEntryPointResource {

	@Related("performance:test")
	Single<PerformanceResultResource> executeRequest(@TemplateVariable("numRequests") Integer numRequests,
			@TemplateVariable("delayMs") Integer delayMs);
}
