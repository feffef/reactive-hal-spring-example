package com.github.feffef.reactivehalspringexample.services.metasearch.services;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.github.feffef.reactivehalspringexample.api.search.SearchResult;
import com.github.feffef.reactivehalspringexample.api.search.SearchResultPageResource;
import com.github.feffef.reactivehalspringexample.api.search.SearchResultResource;

import io.reactivex.rxjava3.annotations.NonNull;
import io.reactivex.rxjava3.core.BackpressureStrategy;
import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.core.FlowableEmitter;
import io.reactivex.rxjava3.core.FlowableOnSubscribe;
import io.reactivex.rxjava3.core.MaybeObserver;
import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.core.SingleObserver;
import io.reactivex.rxjava3.disposables.Disposable;

@Component
public class MetaSearchResultMerger {

	public Flowable<SearchResult> getAllResults(SearchResultPageResource firstPage) {

		return Flowable.create(new PagedResultPulling(firstPage), BackpressureStrategy.BUFFER);
	}

	private static class PagedResultPulling implements FlowableOnSubscribe<SearchResult> {

		private final static Logger log = LoggerFactory.getLogger(PagedResultPulling.class);

		private final SearchResultPageResource firstPage;
		private AtomicInteger pageCount = new AtomicInteger();

		public PagedResultPulling(SearchResultPageResource firstPage) {
			this.firstPage = firstPage;
		}

		@Override
		public void subscribe(@NonNull FlowableEmitter<@NonNull SearchResult> emitter) throws Throwable {

			log.debug("new subscription to paging flowable requesting " + emitter.requested() + " items");

			emitResultsFromPageRecursively(firstPage, emitter);
		}

		private void emitResultsFromPageRecursively(SearchResultPageResource currentPage,
				FlowableEmitter<SearchResult> emitter) {

			Single<List<SearchResult>> resultsOnPage = currentPage.getResults().map(SearchResultResource::getProperties)
					.toList();

			log.debug("retrieving items on page " + pageCount.incrementAndGet());

			resultsOnPage.subscribe(new SingleObserver<List<SearchResult>>() {

				@Override
				public void onSubscribe(@NonNull Disposable d) {
				}

				@Override
				public void onSuccess(@NonNull List<SearchResult> newResults) {

					log.debug(newResults.size() + " items have been retrieved");

					newResults.forEach(emitter::onNext);

					if (emitter.isCancelled()) {
						log.debug("not fetching any more items because the subscriber has cancelled!");
						return;
					}

					if (emitter.requested() > 0) {
						log.debug("have to fetch next page, because " + emitter.requested()
								+ " more items are requested");
						getNextPage(currentPage, emitter);
					} else {
						log.debug("not fetching more pages because no more items are requested");
						emitter.onComplete();
					}
				}

				@Override
				public void onError(@NonNull Throwable e) {
					emitter.onError(e);
				}
			});
		}

		private void getNextPage(SearchResultPageResource currentPage, FlowableEmitter<SearchResult> emitter) {

			currentPage.getNextPage().subscribe(new MaybeObserver<SearchResultPageResource>() {

				@Override
				public void onSubscribe(@NonNull Disposable d) {
				}

				@Override
				public void onSuccess(@NonNull SearchResultPageResource nextPage) {
					log.debug("a next page link was found and more results will be fetched");
					emitResultsFromPageRecursively(nextPage, emitter);
				}

				@Override
				public void onError(@NonNull Throwable e) {
					emitter.onError(e);
				}

				@Override
				public void onComplete() {
					log.debug("no next page link was found");
					emitter.onComplete();
				}
			});
		}
	}

}
