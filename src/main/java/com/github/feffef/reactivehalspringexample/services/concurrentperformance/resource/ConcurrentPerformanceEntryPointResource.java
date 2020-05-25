package com.github.feffef.reactivehalspringexample.services.concurrentperformance.resource;

import com.github.feffef.reactivehalspringexample.api.performance.PerformanceEntryPointResource;
import com.github.feffef.reactivehalspringexample.api.performance.PerformanceResultResource;
import com.github.feffef.reactivehalspringexample.services.concurrentperformance.context.ConcurrentPerformanceRequestContext;
import com.github.feffef.reactivehalspringexample.services.concurrentperformance.controller.ConcurrentPerformanceController;

import io.reactivex.rxjava3.core.Single;
import io.wcm.caravan.hal.microservices.api.server.LinkableResource;
import io.wcm.caravan.hal.resource.Link;

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
		return request.createLinkTo(ConcurrentPerformanceController.class, ctrl -> ctrl.getEntryPoint());
	}

}