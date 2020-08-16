package com.github.feffef.reactivehalspringexample.api.performance;

public interface PerformanceResult {

	int getNumResponses();

	long getTotalResponseMillis();

	long getMeanResponseMillis();

	long getMaxResponseMillis();

	long getMinResponseMillis();

	long getProcessingMillisPerRequest();

	int getNumberOfThreads();
}
