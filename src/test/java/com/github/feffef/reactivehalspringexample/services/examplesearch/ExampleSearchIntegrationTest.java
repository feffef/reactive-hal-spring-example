package com.github.feffef.reactivehalspringexample.services.examplesearch;

import static com.github.feffef.reactivehalspringexample.services.examplesearch.first.FirstSearchResultProvider.MAX_RESULTS_PER_PAGE;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.web.servlet.context.ServletWebServerApplicationContext;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import com.github.feffef.reactivehalspringexample.api.search.SearchEntryPointResource;
import com.github.feffef.reactivehalspringexample.api.search.SearchOptions;
import com.github.feffef.reactivehalspringexample.api.search.SearchResult;
import com.github.feffef.reactivehalspringexample.api.search.SearchResultPageResource;
import com.github.feffef.reactivehalspringexample.api.search.SearchResultResource;
import com.github.feffef.reactivehalspringexample.services.examplesearch.first.FirstSearchController;
import com.google.common.base.Stopwatch;

import io.reactivex.rxjava3.annotations.NonNull;
import io.reactivex.rxjava3.annotations.Nullable;
import io.reactivex.rxjava3.core.Single;
import io.wcm.caravan.rhyme.api.client.HalApiClient;
import io.wcm.caravan.rhyme.api.client.HalResourceLoaderBuilder;
import io.wcm.caravan.rhyme.api.spi.HalResourceLoader;
import io.wcm.caravan.rhyme.testing.spring.MockMvcHalResourceLoaderConfiguration;

@SpringBootTest(properties = "spring.main.allow-bean-definition-overriding=true", webEnvironment = WebEnvironment.RANDOM_PORT)
@ActiveProfiles(ExampleSearchIntegrationTest.PROFILE)
public class ExampleSearchIntegrationTest {

	static final String PROFILE = "example-search-integration-test";

	private static final String QUERY = "foo";

	private final MockFirstSearchResultProvider firstSearchResultProvider;

	private final SearchEntryPointResource searchEntryPoint;

	ExampleSearchIntegrationTest(@Autowired MockFirstSearchResultProvider firstSearchResultProvider,
			@Autowired ServletWebServerApplicationContext server, @Autowired HalResourceLoaderBuilder builder) {

		this.firstSearchResultProvider = firstSearchResultProvider;

		HalResourceLoader nonCachingLoader = builder.build();

		HalApiClient apiClient = HalApiClient.create(nonCachingLoader);

		String entryPointUrl = "http://localhost:" + server.getWebServer().getPort() + FirstSearchController.BASE_PATH;

		this.searchEntryPoint = apiClient.getRemoteResource(entryPointUrl, SearchEntryPointResource.class);
	}

	private SearchEntryPointResource getEntryPoint() {

		return searchEntryPoint;
	}

	private Single<SearchResultPageResource> getFirstPage() {
		return getEntryPoint().executeSearch(QUERY, new SearchOptions());
	}

	private Single<List<SearchResult>> getResultsOnPage(SearchResultPageResource page) {
		return page.getResults().map(SearchResultResource::getProperties).toList();
	}

	@Test
	public void first_page_should_render_if_no_results_are_available() throws Exception {

		firstSearchResultProvider.setNumResultsForQuery(QUERY, 0);

		List<SearchResult> resultsOnFirstPage = getFirstPage().flatMap(this::getResultsOnPage).blockingGet();

		assertThat(resultsOnFirstPage).hasSize(0);
	}

	@Test
	public void first_page_should_contain_max_number_of_results_per_apge() throws Exception {

		firstSearchResultProvider.setNumResultsForQuery(QUERY, MAX_RESULTS_PER_PAGE + 10);

		List<SearchResult> resultsOnFirstPage = getFirstPage().flatMap(this::getResultsOnPage).blockingGet();

		assertThat(resultsOnFirstPage).hasSize(25);
		assertThat(resultsOnFirstPage.get(0).title).isEqualTo("Example Search Result #0");
	}

	@Test
	public void first_page_should_have_no_prev_link() throws Exception {

		firstSearchResultProvider.setNumResultsForQuery(QUERY, MAX_RESULTS_PER_PAGE + 10);

		SearchResultPageResource prevPage = getFirstPage().flatMapMaybe(SearchResultPageResource::getPreviousPage).blockingGet();
		
		assertThat(prevPage).isNull();
	}

	@Test
	public void first_page_should_have_no_next_link_if_all_results_fit_on_one_page() throws Exception {

		firstSearchResultProvider.setNumResultsForQuery(QUERY, MAX_RESULTS_PER_PAGE - 10);

		SearchResultPageResource nextPage = getFirstPage().flatMapMaybe(SearchResultPageResource::getNextPage).blockingGet();
		
		assertThat(nextPage).isNull();
	}

	@Test
	public void first_page_should_have_next_link_to_second_page() throws Exception {

		firstSearchResultProvider.setNumResultsForQuery(QUERY, MAX_RESULTS_PER_PAGE + 10);

		List<SearchResult> resultsOnFirstPage = getFirstPage().flatMapMaybe(SearchResultPageResource::getNextPage)
				.flatMapSingle(this::getResultsOnPage).blockingGet();

		assertThat(resultsOnFirstPage).hasSize(10);
		assertThat(resultsOnFirstPage.get(0).title).isEqualTo("Example Search Result #25");
	}

	@Test
	public void second_page_should_have_prev_link_to_first_page() throws Exception {

		firstSearchResultProvider.setNumResultsForQuery(QUERY, MAX_RESULTS_PER_PAGE + 10);

		List<SearchResult> resultsOnFirstPage = getFirstPage().flatMapMaybe(SearchResultPageResource::getNextPage)
				.flatMap(SearchResultPageResource::getPreviousPage).flatMapSingle(this::getResultsOnPage).blockingGet();

		assertThat(resultsOnFirstPage).hasSize(MAX_RESULTS_PER_PAGE);
		assertThat(resultsOnFirstPage.get(0).title).isEqualTo("Example Search Result #0");
	}

	@Test
	public void second_page_should_have_next_link_to_third_page() throws Exception {

		firstSearchResultProvider.setNumResultsForQuery(QUERY, 2 * MAX_RESULTS_PER_PAGE + 1);

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
