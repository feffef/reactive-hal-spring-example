package com.github.feffef.reactivehalspringexample.services.examplesearch;

import static com.github.feffef.reactivehalspringexample.services.examplesearch.services.ExampleSearchResultProvider.MAX_RESULTS_PER_PAGE;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import com.github.feffef.reactivehalspringexample.api.search.SearchEntryPointResource;
import com.github.feffef.reactivehalspringexample.api.search.SearchOptions;
import com.github.feffef.reactivehalspringexample.api.search.SearchResult;
import com.github.feffef.reactivehalspringexample.api.search.SearchResultPageResource;
import com.github.feffef.reactivehalspringexample.api.search.SearchResultResource;
import com.google.common.base.Stopwatch;

import io.reactivex.rxjava3.core.Single;
import io.wcm.caravan.reha.api.client.HalApiClient;
import io.wcm.caravan.reha.api.common.RequestMetricsCollector;

@SpringBootTest(properties = "spring.main.allow-bean-definition-overriding=true")
@ActiveProfiles(ExampleSearchIntegrationTest.PROFILE)
public class ExampleSearchIntegrationTest {

	static final String PROFILE = "example-search-integration-test";

	private static final String QUERY = "foo";

	private static final String ENTRY_POINT_URI = "/search/example";

	@Autowired
	private io.wcm.caravan.reha.spring.api.MockMvcJsonResourceLoader resourceLoader;

	@Autowired
	MockExampleSearchResultProvider exampleSearchResultProvider;

	private SearchEntryPointResource getEntryPoint() {

		HalApiClient apiClient = HalApiClient.create(resourceLoader, RequestMetricsCollector.create());

		return apiClient.getEntryPoint(ENTRY_POINT_URI, SearchEntryPointResource.class);
	}

	private Single<SearchResultPageResource> getFirstPage() {
		return getEntryPoint().executeSearch(QUERY, new SearchOptions());
	}

	private Single<List<SearchResult>> getResultsOnPage(SearchResultPageResource page) {
		return page.getResults().map(SearchResultResource::getProperties).toList();
	}

	@Test
	public void first_page_should_render_if_no_results_are_available() throws Exception {

		exampleSearchResultProvider.setNumResultsForQuery(QUERY, 0);

		List<SearchResult> resultsOnFirstPage = getFirstPage().flatMap(this::getResultsOnPage).blockingGet();

		assertThat(resultsOnFirstPage).hasSize(0);
	}

	@Test
	public void first_page_should_contain_max_number_of_results_per_apge() throws Exception {

		exampleSearchResultProvider.setNumResultsForQuery(QUERY, MAX_RESULTS_PER_PAGE + 10);

		List<SearchResult> resultsOnFirstPage = getFirstPage().flatMap(this::getResultsOnPage).blockingGet();

		assertThat(resultsOnFirstPage).hasSize(25);
		assertThat(resultsOnFirstPage.get(0).title).isEqualTo("Example Search Result #0");
	}

	@Test
	public void first_page_should_have_no_prev_link() throws Exception {

		exampleSearchResultProvider.setNumResultsForQuery(QUERY, MAX_RESULTS_PER_PAGE + 10);

		getFirstPage().flatMapMaybe(SearchResultPageResource::getPreviousPage).test().assertComplete().assertNoValues();
	}

	@Test
	public void first_page_should_have_no_next_link_if_all_results_fit_on_one_page() throws Exception {

		exampleSearchResultProvider.setNumResultsForQuery(QUERY, MAX_RESULTS_PER_PAGE - 10);

		getFirstPage().flatMapMaybe(SearchResultPageResource::getNextPage).test().assertComplete().assertNoValues();
	}

	@Test
	public void first_page_should_have_next_link_to_second_page() throws Exception {

		exampleSearchResultProvider.setNumResultsForQuery(QUERY, MAX_RESULTS_PER_PAGE + 10);

		List<SearchResult> resultsOnFirstPage = getFirstPage().flatMapMaybe(SearchResultPageResource::getNextPage)
				.flatMapSingle(this::getResultsOnPage).blockingGet();

		assertThat(resultsOnFirstPage).hasSize(10);
		assertThat(resultsOnFirstPage.get(0).title).isEqualTo("Example Search Result #25");
	}

	@Test
	public void second_page_should_have_prev_link_to_first_page() throws Exception {

		exampleSearchResultProvider.setNumResultsForQuery(QUERY, MAX_RESULTS_PER_PAGE + 10);

		List<SearchResult> resultsOnFirstPage = getFirstPage().flatMapMaybe(SearchResultPageResource::getNextPage)
				.flatMap(SearchResultPageResource::getPreviousPage).flatMapSingle(this::getResultsOnPage).blockingGet();

		assertThat(resultsOnFirstPage).hasSize(MAX_RESULTS_PER_PAGE);
		assertThat(resultsOnFirstPage.get(0).title).isEqualTo("Example Search Result #0");
	}

	@Test
	public void second_page_should_have_next_link_to_third_page() throws Exception {

		exampleSearchResultProvider.setNumResultsForQuery(QUERY, 2 * MAX_RESULTS_PER_PAGE + 1);

		List<SearchResult> resultsOnFirstPage = getFirstPage().flatMapMaybe(SearchResultPageResource::getNextPage)
				.flatMap(SearchResultPageResource::getNextPage).flatMapSingle(this::getResultsOnPage).blockingGet();

		assertThat(resultsOnFirstPage).hasSize(1);
		assertThat(resultsOnFirstPage.get(0).title).isEqualTo("Example Search Result #50");
	}

	@Test
	public void delayMs_search_option_should_be_respected() {

		SearchOptions options = new SearchOptions();
		options.delayMs = 100;

		Stopwatch sw = Stopwatch.createStarted();
		getEntryPoint().executeSearch(QUERY, options).flatMap(this::getResultsOnPage).blockingGet();

		assertThat(sw.elapsed(TimeUnit.MILLISECONDS)).isGreaterThanOrEqualTo(options.delayMs);
	}
}
