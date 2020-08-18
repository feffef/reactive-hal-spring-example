package com.github.feffef.reactivehalspringexample.api.search;

import io.reactivex.rxjava3.core.Maybe;
import io.reactivex.rxjava3.core.Single;
import io.wcm.caravan.reha.api.annotations.HalApiInterface;
import io.wcm.caravan.reha.api.annotations.RelatedResource;
import io.wcm.caravan.reha.api.annotations.TemplateVariable;
import io.wcm.caravan.reha.api.annotations.TemplateVariables;
import io.wcm.caravan.reha.api.relations.StandardRelations;

@HalApiInterface
public interface SearchEntryPointResource {

	@RelatedResource(relation = StandardRelations.SEARCH)
	Single<SearchResultPageResource> executeSearch(@TemplateVariable("query") String query,
			@TemplateVariables SearchOptions options);

	@RelatedResource(relation = StandardRelations.MEMENTO)
	Maybe<SearchEntryPointResource> getImmutableEntryPoint(@TemplateVariable("queryTimestamp") String queryTimestamp);
}
