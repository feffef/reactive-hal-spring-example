package com.github.feffef.reactivehalspringexample.services.concurrentperformance.context;

import com.github.feffef.reactivehalspringexample.api.search.SearchResult;
import com.github.feffef.reactivehalspringexample.common.HalServiceRequestContext;

import io.reactivex.rxjava3.core.Observable;

public interface ConcurrentPerformanceRequestContext extends HalServiceRequestContext {

	Observable<SearchResult> getSearchResults(String query, int delayMs);
}
