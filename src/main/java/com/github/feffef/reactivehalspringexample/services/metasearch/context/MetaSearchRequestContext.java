package com.github.feffef.reactivehalspringexample.services.metasearch.context;

import com.github.feffef.reactivehalspringexample.api.search.SearchOptions;
import com.github.feffef.reactivehalspringexample.api.search.SearchResult;
import com.github.feffef.reactivehalspringexample.common.HalServiceRequestContext;

import io.reactivex.rxjava3.core.Flowable;

public interface MetaSearchRequestContext extends HalServiceRequestContext {

	Flowable<SearchResult> getAllGoogleResults(String query, SearchOptions options);
}
