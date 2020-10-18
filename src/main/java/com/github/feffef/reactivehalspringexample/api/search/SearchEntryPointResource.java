package com.github.feffef.reactivehalspringexample.api.search;

import io.reactivex.rxjava3.core.Maybe;
import io.reactivex.rxjava3.core.Single;
import io.wcm.caravan.rhyme.api.annotations.HalApiInterface;
import io.wcm.caravan.rhyme.api.annotations.Related;
import io.wcm.caravan.rhyme.api.annotations.TemplateVariable;
import io.wcm.caravan.rhyme.api.annotations.TemplateVariables;
import io.wcm.caravan.rhyme.api.relations.StandardRelations;

@HalApiInterface
public interface SearchEntryPointResource {

	@Related(StandardRelations.SEARCH)
	Single<SearchResultPageResource> executeSearch(@TemplateVariable("query") String query,
			@TemplateVariables SearchOptions options);

	@Related(StandardRelations.MEMENTO)
	Maybe<SearchEntryPointResource> getImmutableEntryPoint(@TemplateVariable("queryTimestamp") String queryTimestamp);
}
