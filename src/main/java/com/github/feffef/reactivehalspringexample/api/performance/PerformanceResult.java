package com.github.feffef.reactivehalspringexample.api.performance;

public interface PerformanceResult {

	int getNumResponses();

	long getTotalResponseTime();

	long getMeanResponseTime();

	long getMaxResponseTime();

	long getMinResponseTime();

	int getNumberOfThreads();
}
