package com.github.feffef.reactivehalspringexample.services.common.context;

import com.github.feffef.reactivehalspringexample.common.SpringRehaRequestContext;
import com.github.feffef.reactivehalspringexample.services.common.controller.SearchProviderController;
import com.github.feffef.reactivehalspringexample.services.common.services.SearchResultProvider;

public interface SearchProviderRequestContext extends SpringRehaRequestContext<SearchProviderController> {

	SearchResultProvider getSearchResultProvider();
}
