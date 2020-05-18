package com.github.feffef.reactivehalspringexample.api.search;

import io.reactivex.rxjava3.core.Single;
import io.wcm.caravan.hal.api.annotations.HalApiInterface;
import io.wcm.caravan.hal.api.annotations.RelatedResource;
import io.wcm.caravan.hal.api.annotations.TemplateVariable;
import io.wcm.caravan.hal.api.annotations.TemplateVariables;

@HalApiInterface
public interface SearchEntryPointResource {

	@RelatedResource(relation = "search:resultPage")
	Single<SearchResultPageResource> executeSearch(@TemplateVariable("query") String query,
			@TemplateVariables SearchOptions options);
}
