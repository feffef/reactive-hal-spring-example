package com.github.feffef.reactivehalspringexample.services.metasearch.services;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;

import com.github.feffef.reactivehalspringexample.api.search.SearchResult;
import com.github.feffef.reactivehalspringexample.api.search.SearchResultPageResource;
import com.github.feffef.reactivehalspringexample.api.search.SearchResultResource;
import com.google.common.collect.Iterables;

import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.core.Maybe;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.subjects.MaybeSubject;
import io.reactivex.rxjava3.subscribers.TestSubscriber;

public class MetaSearchResultMergerTest {

	private static final int RESULTS_PER_PAGE = 10;

	class StaticTestResultPage implements SearchResultPageResource {

		private final int resultsLeft;
		private final int resultsPerPage;

		public StaticTestResultPage(int resultsLeft, int resultsPerPage) {
			this.resultsLeft = resultsLeft;
			this.resultsPerPage = resultsPerPage;
		}

		@Override
		public Observable<SearchResultResource> getResults() {

			int resultsOnPage = Math.min(resultsLeft, resultsPerPage);

			return Observable.range(0, resultsOnPage).map(i -> new SearchResultResource() {

				@Override
				public SearchResult getProperties() {
					SearchResult result = new SearchResult();
					result.title = "Result #" + i;
					return result;
				}
			});
		}

		@Override
		public Maybe<SearchResultPageResource> getNextPage() {
			if (resultsLeft <= resultsPerPage) {
				return Maybe.empty();
			}
			return Maybe.just(new StaticTestResultPage(resultsLeft - resultsPerPage, resultsPerPage));
		}

		@Override
		public Maybe<SearchResultPageResource> getPreviousPage() {
			throw new UnsupportedOperationException("not implemented");
		}

	}

	class AsyncResultMock {

		int nextEmissionIndex = 0;
		List<MaybeSubject<MockedSearchResult>> mockedResults = new ArrayList<>();

		private AsyncResultMock() {

		}

		public MaybeSubject<MockedSearchResult> getSubject(int i) {
			return mockedResults.get(i);
		}

		int getNumSubjects() {
			return mockedResults.size();
		}

		SearchResultPageResource getFirstPage() {
			return createNextPage();
		}

		SearchResultPageResource createNextPage() {
			mockedResults.add(MaybeSubject.create());

			return new MockedSearchResultPage(Iterables.getLast(mockedResults));
		}

		void emitNextPage(int numResults) {
			MaybeSubject<MockedSearchResult> subject = mockedResults.get(nextEmissionIndex++);
			subject.onSuccess(new MockedSearchResult(numResults, true));
		}

		void emitLastPage(int numResults) {
			MaybeSubject<MockedSearchResult> subject = mockedResults.get(nextEmissionIndex++);
			subject.onSuccess(new MockedSearchResult(numResults, false));
		}

		class MockedSearchResult {
			final int numResultsOnPage;
			final boolean hasMorePages;

			MockedSearchResult(int numResultsOnPage, boolean hasMorePages) {
				this.numResultsOnPage = numResultsOnPage;
				this.hasMorePages = hasMorePages;
			}
		}

		class MockedSearchResultPage implements SearchResultPageResource {

			private final Maybe<MockedSearchResult> mock;

			public MockedSearchResultPage(Maybe<MockedSearchResult> mock) {
				this.mock = mock;
			}

			@Override
			public Observable<SearchResultResource> getResults() {

				return mock.flatMapObservable(
						m -> Observable.range(0, m.numResultsOnPage).map(i -> new SearchResultResource() {

							@Override
							public SearchResult getProperties() {
								SearchResult result = new SearchResult();
								result.title = "Result #" + i;
								return result;
							}
						}));
			}

			@Override
			public Maybe<SearchResultPageResource> getNextPage() {
				return mock.flatMap(m -> {
					if (m.hasMorePages) {
						return Maybe.just(createNextPage());
					}
					return Maybe.empty();
				});
			}

			@Override
			public Maybe<SearchResultPageResource> getPreviousPage() {
				throw new UnsupportedOperationException("not implemented");
			}

		}

	}

	private Flowable<SearchResult> getAllResults(int numResults) {
		MetaSearchResultMerger merger = new MetaSearchResultMerger();

		SearchResultPageResource firstPage = new StaticTestResultPage(numResults, RESULTS_PER_PAGE);

		Flowable<SearchResult> allResults = merger.getAllResults(firstPage);

		return allResults;
	}

	private void assertThatBlockingListGetReturnsAllResults(int numResults) {
		Flowable<SearchResult> allResults = getAllResults(numResults);

		assertThat(allResults.toList().blockingGet()).hasSize(numResults);
	}

	@Test
	public void blocking_get_on_all_results_works_for_empty_single_page() throws Exception {

		assertThatBlockingListGetReturnsAllResults(0);
	}

	@Test
	public void blocking_get_on_all_results_works_for_half_a_page() throws Exception {

		assertThatBlockingListGetReturnsAllResults(5);
	}

	@Test
	public void blocking_get_on_all_results_works_for_one_full_page() throws Exception {

		assertThatBlockingListGetReturnsAllResults(10);
	}

	@Test
	public void blocking_get_on_all_results_works_for_one_and_a_half_pages() throws Exception {

		assertThatBlockingListGetReturnsAllResults(15);
	}

	@Test
	public void blocking_get_on_all_results_works_for_two_full_pages() throws Exception {

		assertThatBlockingListGetReturnsAllResults(20);
	}

	@Test
	public void blocking_get_on_all_results_works_for_two_and_a_half_pages() throws Exception {

		assertThatBlockingListGetReturnsAllResults(25);
	}

	@Test
	public void blocking_get_on_all_results_works_for_three_full_pages() throws Exception {

		assertThatBlockingListGetReturnsAllResults(30);
	}

	@Test
	public void paged_results_should_be_emitted_immediately() throws Exception {

		AsyncResultMock arm = new AsyncResultMock();

		MetaSearchResultMerger merger = new MetaSearchResultMerger();
		Flowable<SearchResult> allResults = merger.getAllResults(arm.getFirstPage());

		assertThat(arm.getNumSubjects()).isEqualTo(1);
		assertThat(arm.getSubject(0).hasObservers()).isEqualTo(false);

		TestSubscriber<SearchResult> subscriber = allResults.test();
		subscriber.assertNoValues();
		assertThat(arm.getNumSubjects()).isEqualTo(1);
		assertThat(arm.getSubject(0).hasObservers()).isEqualTo(true);

		arm.emitNextPage(10);
		subscriber.assertValueCount(10);
		assertThat(arm.getNumSubjects()).isEqualTo(2);
		assertThat(arm.getSubject(1).hasObservers()).isEqualTo(true);

		arm.emitNextPage(5);
		subscriber.assertValueCount(15);
		assertThat(arm.getNumSubjects()).isEqualTo(3);
		assertThat(arm.getSubject(2).hasObservers()).isEqualTo(true);

		arm.emitLastPage(5);
		subscriber.assertValueCount(20);
		assertThat(arm.getNumSubjects()).isEqualTo(3);
		subscriber.assertComplete();
	}

	@Test
	public void paged_results_should_respect_limit_operator() throws Exception {

		AsyncResultMock arm = new AsyncResultMock();

		MetaSearchResultMerger merger = new MetaSearchResultMerger();
		Flowable<SearchResult> allResults = merger.getAllResults(arm.getFirstPage());

		Flowable<SearchResult> limitedResults = allResults.take(5);

		assertThat(arm.getNumSubjects()).isEqualTo(1);
		assertThat(arm.getSubject(0).hasObservers()).isEqualTo(false);

		TestSubscriber<SearchResult> subscriber = limitedResults.test();
		subscriber.assertNoValues();
		assertThat(arm.getNumSubjects()).isEqualTo(1);
		assertThat(arm.getSubject(0).hasObservers()).isEqualTo(true);

		arm.emitNextPage(10);

		assertThat(arm.getNumSubjects()).isEqualTo(1);
	}
}
