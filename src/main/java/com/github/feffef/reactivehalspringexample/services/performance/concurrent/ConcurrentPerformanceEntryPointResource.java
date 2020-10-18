package com.github.feffef.reactivehalspringexample.services.performance.concurrent;

import com.github.feffef.reactivehalspringexample.api.performance.PerformanceEntryPointResource;
import com.github.feffef.reactivehalspringexample.api.performance.PerformanceResultResource;

import io.reactivex.rxjava3.core.Single;
import io.wcm.caravan.hal.resource.Link;
import io.wcm.caravan.rhyme.api.resources.LinkableResource;

public class ConcurrentPerformanceEntryPointResource implements PerformanceEntryPointResource, LinkableResource {

	private final ConcurrentPerformanceRequestContext request;

	public ConcurrentPerformanceEntryPointResource(ConcurrentPerformanceRequestContext request) {
		this.request = request;
	}

	@Override
	public Single<PerformanceResultResource> executeRequest(Integer numRequests, Integer delayMs) {

		return Single.just(new ConcurrentPerformanceResultResource(request, numRequests, delayMs));
	}

	@Override
	public Link createLink() {

		return request.createLinkTo(ctrl -> ctrl.getEntryPoint())
				.setTitle("Entry point of the " + ConcurrentPerformanceController.BASE_PATH + " test service");
	}

}
