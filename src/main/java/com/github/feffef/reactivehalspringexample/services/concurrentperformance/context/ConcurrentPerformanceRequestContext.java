package com.github.feffef.reactivehalspringexample.services.concurrentperformance.context;

import com.github.feffef.reactivehalspringexample.api.search.SearchResult;
import com.github.feffef.reactivehalspringexample.common.SpringRehaRequestContext;
import com.github.feffef.reactivehalspringexample.services.concurrentperformance.controller.ConcurrentPerformanceController;

import io.reactivex.rxjava3.core.Observable;

public interface ConcurrentPerformanceRequestContext extends SpringRehaRequestContext<ConcurrentPerformanceController> {

	Observable<SearchResult> getSearchResults(String query, int delayMs);
}
