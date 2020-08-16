package com.github.feffef.reactivehalspringexample.services.googlesearch.services;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.util.UriComponentsBuilder;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.github.feffef.reactivehalspringexample.api.search.SearchOptions;
import com.github.feffef.reactivehalspringexample.api.search.SearchResult;
import com.github.feffef.reactivehalspringexample.services.common.services.SearchProviderResult;
import com.github.feffef.reactivehalspringexample.services.common.services.SearchResultProvider;

import io.reactivex.rxjava3.core.Single;
import io.wcm.caravan.reha.api.common.HalResponse;
import io.wcm.caravan.reha.api.exceptions.HalApiDeveloperException;
import io.wcm.caravan.reha.api.exceptions.HalApiServerException;
import io.wcm.caravan.reha.spring.impl.WebClientJsonResourceLoader;

@Service
public class GoogleSearchResultProvider implements SearchResultProvider {

	private static final Logger log = LoggerFactory.getLogger(GoogleSearchResultProvider.class);

	public static final int MAX_RESULTS_PER_PAGE = 10;

	private static final String API_KEY = "AIzaSyC_FALX9-v7EkxqqaB7a-DBt2qX8Ibx3IU";
	private static final String SEARCH_ENGINE_ID = "011960927638221306647:9gonapa1yw0";

	private static final String GOOGLE_CSE_URL = "https://customsearch.googleapis.com/customsearch/v1";

	@Autowired
	private WebClientJsonResourceLoader resourceLoader;

	@Override
	public Single<SearchProviderResult> getResults(String query, int startIndex, SearchOptions options) {

		if (query == null) {
			return Single.error(new HalApiDeveloperException("null query was given"));
		}

		String requestUrl = buildGoogleCseRequestUri(query, startIndex);

		return resourceLoader.loadJsonResource(requestUrl).map(this::toSearchProviderResult).onErrorResumeNext(
				ex -> Single.error(new HalApiServerException(503, "The Google Search API request failed", ex)));
	}

	private String buildGoogleCseRequestUri(String query, int startIndex) {

		String uri = UriComponentsBuilder.fromHttpUrl(GOOGLE_CSE_URL).queryParam("cx", SEARCH_ENGINE_ID)
				.queryParam("key", API_KEY).queryParam("q", query).queryParam("start", startIndex).build()
				.toUriString();

		log.info("Constructed Google query URI: " + uri);

		return uri;
	}

	private SearchProviderResult toSearchProviderResult(HalResponse response) {

		ObjectNode resultJson = response.getBody().getModel();

		int totalNumResults = resultJson.at("/queries/request/0/totalResults").asInt(0);

		List<SearchResult> results = StreamSupport.stream(resultJson.at("/items").spliterator(), false)
				.map(this::parseResultItem).collect(Collectors.toList());

		return new SearchProviderResult() {

			@Override
			public int getTotalNumResults() {
				return totalNumResults;
			}

			@Override
			public List<SearchResult> getResultsOnPage() {
				return results;
			}

		};
	}

	private SearchResult parseResultItem(JsonNode item) {
		SearchResult result = new SearchResult();

		result.title = item.path("title").asText();
		result.url = item.path("link").asText();

		return result;
	}

	@Override
	public int getMaxResultsPerPage() {
		return MAX_RESULTS_PER_PAGE;
	}

	@Override
	public SearchOptions getDefaultOptions() {
		SearchOptions options = new SearchOptions();
		options.delayMs = 0;
		return options;
	}

}
