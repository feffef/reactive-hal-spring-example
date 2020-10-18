package com.github.feffef.reactivehalspringexample.api.search;

import io.reactivex.rxjava3.core.Maybe;
import io.reactivex.rxjava3.core.Observable;
import io.wcm.caravan.reha.api.annotations.HalApiInterface;
import io.wcm.caravan.reha.api.annotations.Related;
import io.wcm.caravan.reha.api.relations.StandardRelations;

@HalApiInterface
public interface SearchResultPageResource {

	@Related(StandardRelations.ITEM)
	Observable<SearchResultResource> getResults();

	@Related(StandardRelations.NEXT)
	Maybe<SearchResultPageResource> getNextPage();

	@Related(StandardRelations.PREV)
	Maybe<SearchResultPageResource> getPreviousPage();

}
