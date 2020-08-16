package com.github.feffef.reactivehalspringexample.services.metasearch;

import org.apache.commons.lang3.ObjectUtils;

import com.github.feffef.reactivehalspringexample.api.search.SearchOptions;
import com.github.feffef.reactivehalspringexample.api.search.SearchResult;
import com.github.feffef.reactivehalspringexample.api.search.SearchResultPageResource;
import com.github.feffef.reactivehalspringexample.api.search.SearchResultResource;
import com.github.feffef.reactivehalspringexample.common.resources.SearchResultResourceImpl;

import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.core.Maybe;
import io.reactivex.rxjava3.core.Observable;
import io.wcm.caravan.hal.resource.Link;
import io.wcm.caravan.reha.api.resources.LinkableResource;

public class MetaSearchResultPageResource implements SearchResultPageResource, LinkableResource {

	private final static int RESULTS_PER_PAGE = 10;

	private final MetaSearchRequestContext request;

	private final String query;
	private final SearchOptions options;
	private final Integer startIndex;

	private Flowable<SearchResult> resultsOnPagePlusOne;

	public MetaSearchResultPageResource(MetaSearchRequestContext request, String query, SearchOptions options,
			Integer startIndex) {
		this.request = request;
		this.query = query;
		this.options = ObjectUtils.defaultIfNull(options, new SearchOptions());
		this.startIndex = startIndex;

		if (query == null) {
			this.resultsOnPagePlusOne = Flowable.empty();
		} else {
			this.resultsOnPagePlusOne = getResultsOnPagePlusOneMore().cache();
		}
	}

	private Flowable<SearchResult> getResultsOnPagePlusOneMore() {

		Flowable<SearchResult> mergedResults = request.fetchAndMergeResults(query, options);

		return mergedResults.skip(startIndex).take(RESULTS_PER_PAGE + 1);
	}

	@Override
	public Observable<SearchResultResource> getResults() {

		return resultsOnPagePlusOne.take(RESULTS_PER_PAGE).toObservable().map(SearchResultResourceImpl::new);
	}

	@Override
	public Maybe<SearchResultPageResource> getNextPage() {

		int nextStartIndex = startIndex + RESULTS_PER_PAGE;

		Maybe<SearchResult> firstResultOnNextPage = resultsOnPagePlusOne.skip(RESULTS_PER_PAGE).firstElement();

		return firstResultOnNextPage.flatMap((firstResult) -> linkToPage(nextStartIndex));
	}

	@Override
	public Maybe<SearchResultPageResource> getPreviousPage() {

		int prevStartIndex = startIndex - RESULTS_PER_PAGE;

		return linkToPage(prevStartIndex);
	}

	private Maybe<SearchResultPageResource> linkToPage(int index) {
		if (index < 0) {
			return Maybe.empty();
		}
		return Maybe.just(new MetaSearchResultPageResource(request, query, options, index));
	}

	@Override
	public Link createLink() {

		return request.createLinkTo(
				ctrl -> ctrl.getResultPage(query, options.delayMs, options.skipFirst, options.skipSecond, startIndex))
				.setTitle(getLinkTitle());
	}

	private String getLinkTitle() {

		if (query == null) {
			return "Execute a search query using with multiple search services and merge the result";
		}

		int pageIndex = startIndex / RESULTS_PER_PAGE;
		int lastIndex = startIndex + RESULTS_PER_PAGE - 1;

		return "Page " + pageIndex + " with results " + startIndex + "-" + lastIndex + " from the meta search service";
	}
}
