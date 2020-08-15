package com.github.feffef.reactivehalspringexample.services.metasearch.context;

import com.github.feffef.reactivehalspringexample.api.search.SearchOptions;
import com.github.feffef.reactivehalspringexample.api.search.SearchResult;
import com.github.feffef.reactivehalspringexample.common.SpringRehaRequestContext;
import com.github.feffef.reactivehalspringexample.services.metasearch.controller.MetaSearchController;

import io.reactivex.rxjava3.core.Flowable;

public interface MetaSearchRequestContext extends SpringRehaRequestContext<MetaSearchController> {

	Flowable<SearchResult> getAllExampleResults(String query, SearchOptions options);
}
