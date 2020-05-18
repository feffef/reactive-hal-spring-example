package com.github.feffef.reactivehalspringexample.services.metasearch.context;

import com.github.feffef.reactivehalspringexample.api.search.SearchEntryPointResource;
import com.github.feffef.reactivehalspringexample.common.HalServiceRequestContext;

public interface MetaSearchRequestContext extends HalServiceRequestContext {

	SearchEntryPointResource getGoogleSearchEntryPoint();
}
