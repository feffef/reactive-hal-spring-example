package com.github.feffef.reactivehalspringexample.api.search;

import io.reactivex.rxjava3.core.Maybe;
import io.reactivex.rxjava3.core.Observable;
import io.wcm.caravan.hal.api.annotations.HalApiInterface;
import io.wcm.caravan.hal.api.annotations.RelatedResource;
import io.wcm.caravan.hal.api.relations.StandardRelations;

@HalApiInterface
public interface SearchResultPageResource {

	@RelatedResource(relation = StandardRelations.ITEM)
	Observable<SearchResultResource> getResults();

	@RelatedResource(relation = StandardRelations.NEXT)
	Maybe<SearchResultPageResource> getNextPage();

	@RelatedResource(relation = StandardRelations.PREV)
	Maybe<SearchResultPageResource> getPreviousPage();

}
