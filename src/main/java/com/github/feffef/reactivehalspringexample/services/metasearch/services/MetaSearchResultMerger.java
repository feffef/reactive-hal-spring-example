package com.github.feffef.reactivehalspringexample.services.metasearch.services;

import java.util.List;

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

		private final SearchResultPageResource firstPage;

		public PagedResultPulling(SearchResultPageResource firstPage) {
			this.firstPage = firstPage;
		}

		@Override
		public void subscribe(@NonNull FlowableEmitter<@NonNull SearchResult> emitter) throws Throwable {

			emitResultsFromPageRecursively(firstPage, emitter);
		}

		private void emitResultsFromPageRecursively(SearchResultPageResource currentPage,
				FlowableEmitter<SearchResult> emitter) {

			Single<List<SearchResult>> resultsOnPage = currentPage.getResults().map(SearchResultResource::getProperties)
					.toList();

			resultsOnPage.subscribe(new SingleObserver<List<SearchResult>>() {

				@Override
				public void onSubscribe(@NonNull Disposable d) {
				}

				@Override
				public void onSuccess(@NonNull List<SearchResult> newResults) {

					newResults.forEach(emitter::onNext);

					if (emitter.requested() > 0) {
						getNextPage(currentPage, emitter);
					} else {
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
					emitResultsFromPageRecursively(nextPage, emitter);
				}

				@Override
				public void onError(@NonNull Throwable e) {
					emitter.onError(e);
				}

				@Override
				public void onComplete() {
					emitter.onComplete();
				}
			});
		}
	}
}
