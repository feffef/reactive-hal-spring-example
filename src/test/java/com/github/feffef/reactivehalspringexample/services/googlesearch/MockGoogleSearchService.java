package com.github.feffef.reactivehalspringexample.services.googlesearch;

import java.util.HashMap;
import java.util.Map;

import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import com.github.feffef.reactivehalspringexample.services.googlesearch.services.GoogleSearchService;

@Component
@Primary // this service should *replace* the real GoogleSearchService...
@Profile(GoogleSearchIntegrationTest.PROFILE) //... in integration tests only
public class MockGoogleSearchService extends GoogleSearchService {

	private final Map<String, Integer> numResultsMap = new HashMap<>();

	void setNumResultsForQuery(String query, int numResults) {
		numResultsMap.put(query, numResults);
	}

	@Override
	public int getTotalNumResults(String query) {

		if (!numResultsMap.containsKey(query)) {
			throw new RuntimeException("Number of results for query " + query + " was not set");
		}

		return numResultsMap.get(query);
	}

}
