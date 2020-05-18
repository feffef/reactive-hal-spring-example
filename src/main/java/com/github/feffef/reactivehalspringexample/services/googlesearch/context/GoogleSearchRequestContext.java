package com.github.feffef.reactivehalspringexample.services.googlesearch.context;

import com.github.feffef.reactivehalspringexample.common.HalServiceRequestContext;
import com.github.feffef.reactivehalspringexample.services.googlesearch.services.GoogleSearchService;

public interface GoogleSearchRequestContext extends HalServiceRequestContext {

	GoogleSearchService getSearchService();

}
