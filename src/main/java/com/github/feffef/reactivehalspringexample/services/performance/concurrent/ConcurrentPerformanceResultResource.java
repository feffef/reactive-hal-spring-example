package com.github.feffef.reactivehalspringexample.services.performance.concurrent;

import java.lang.management.ManagementFactory;
import java.lang.management.ThreadMXBean;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.apache.commons.lang3.RandomStringUtils;

import com.github.feffef.reactivehalspringexample.api.performance.PerformanceResult;
import com.github.feffef.reactivehalspringexample.api.performance.PerformanceResultResource;
import com.github.feffef.reactivehalspringexample.services.metasearch.MetaSearchController;
import com.google.common.base.Stopwatch;

import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.Single;
import io.wcm.caravan.hal.resource.Link;
import io.wcm.caravan.reha.api.resources.LinkableResource;

public class ConcurrentPerformanceResultResource implements PerformanceResultResource, LinkableResource {

	private final static ThreadMXBean THREAD_MAX_BEAN = ManagementFactory.getThreadMXBean();

	private final ConcurrentPerformanceRequestContext request;
	private final Integer numRequests;
	private final Integer delayMs;

	private final Stopwatch stopwatch = Stopwatch.createStarted();

	public ConcurrentPerformanceResultResource(ConcurrentPerformanceRequestContext request, Integer numRequests,
			Integer delayMs) {
		this.request = request;
		this.numRequests = numRequests;
		this.delayMs = delayMs;

	}

	@Override
	public Single<PerformanceResult> getProperties() {

		Observable<PerformanceResult> allResults = Observable.range(0, numRequests)
				.flatMapSingle(this::executeAndMeasureRequest);

		return allResults.toList().map(this::getOverallResult);
	}

	private PerformanceResult getOverallResult(List<PerformanceResult> results) {

		Long totalResponseTime = stopwatch.elapsed(TimeUnit.MILLISECONDS);

		Optional<Integer> maxNumberOfThreads = results.stream().map(PerformanceResult::getNumberOfThreads)
				.reduce(Math::max);

		Double meanResponseTime = results.stream()
				.collect(Collectors.averagingLong(PerformanceResult::getTotalResponseMillis));

		Optional<Long> maxResponseTime = results.stream().map(PerformanceResult::getTotalResponseMillis)
				.reduce(Math::max);

		Optional<Long> minResponseTime = results.stream().map(PerformanceResult::getTotalResponseMillis)
				.reduce(Math::min);

		return new PerformanceResult() {

			@Override
			public long getTotalResponseMillis() {
				return totalResponseTime;
			}

			@Override
			public int getNumberOfThreads() {
				return maxNumberOfThreads.get();
			}

			@Override
			public long getMeanResponseMillis() {
				return Math.round(meanResponseTime);
			}

			@Override
			public int getNumResponses() {
				return results.size();
			}

			@Override
			public long getMaxResponseMillis() {
				return maxResponseTime.get();
			}

			@Override
			public long getMinResponseMillis() {
				return minResponseTime.get();
			}

			@Override
			public long getProcessingMillisPerRequest() {
				return (totalResponseTime - delayMs) / numRequests;
			}
		};
	}

	private Single<PerformanceResult> executeAndMeasureRequest(int index) {

		Stopwatch sw = Stopwatch.createStarted();
		String query = RandomStringUtils.randomAlphabetic(25);

		return request.getSearchResults(query, delayMs).toList().map(results -> {

			long responseTime = sw.elapsed(TimeUnit.MILLISECONDS);

			return new PerformanceResult() {

				@Override
				public int getNumResponses() {
					return 1;
				}

				@Override
				public long getTotalResponseMillis() {
					return responseTime;
				}

				@Override
				public long getMinResponseMillis() {
					return responseTime;
				}

				@Override
				public long getMeanResponseMillis() {
					return responseTime;
				}

				@Override
				public long getMaxResponseMillis() {
					return responseTime;
				}

				@Override
				public int getNumberOfThreads() {
					return THREAD_MAX_BEAN.getThreadCount();
				}

				@Override
				public long getProcessingMillisPerRequest() {
					return responseTime - delayMs;
				}

			};
		});
	}

	@Override
	public Link createLink() {

		return request.createLinkTo(ctrl -> ctrl.getResult(numRequests, delayMs)).setTitle(getLinkTitle());
	}

	private String getLinkTitle() {

		if (numRequests == null) {
			return "Execute a number of parallel requests with random search terms to the "
					+ MetaSearchController.BASE_PATH + " service";
		}

		return "Execution results for " + numRequests + " parallel requests to the " + MetaSearchController.BASE_PATH
				+ " service";
	}

}
