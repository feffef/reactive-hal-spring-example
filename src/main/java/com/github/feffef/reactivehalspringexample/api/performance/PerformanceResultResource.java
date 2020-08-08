package com.github.feffef.reactivehalspringexample.api.performance;

import io.reactivex.rxjava3.core.Single;
import io.wcm.caravan.reha.api.annotations.HalApiInterface;
import io.wcm.caravan.reha.api.annotations.ResourceState;

@HalApiInterface
public interface PerformanceResultResource {

	@ResourceState
	Single<PerformanceResult> getProperties();
}
