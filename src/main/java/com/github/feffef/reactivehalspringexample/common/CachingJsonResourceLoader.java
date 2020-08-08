package com.github.feffef.reactivehalspringexample.common;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;

import io.reactivex.rxjava3.core.Single;
import io.wcm.caravan.reha.api.client.JsonResourceLoader;
import io.wcm.caravan.reha.api.common.HalResponse;

@Component
public class CachingJsonResourceLoader implements JsonResourceLoader {

	@Autowired
	private WebClientJsonResourceLoader webClientLoader;

	@Override
	@Cacheable("CachingJsonResourceLoader")
	public Single<HalResponse> loadJsonResource(String uri) {

		return webClientLoader.loadJsonResource(uri).cache();
	}

}
