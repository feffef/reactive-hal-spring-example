package com.github.feffef.reactivehalspringexample.common;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;

import com.github.feffef.reactivehalspringexample.CacheCustomizer;

import io.reactivex.rxjava3.core.Single;
import io.wcm.caravan.hal.microservices.api.client.JsonResourceLoader;
import io.wcm.caravan.hal.microservices.api.common.HalResponse;

@Component
public class CachingJsonResourceLoader implements JsonResourceLoader {

	@Autowired
	private WebClientJsonResourceLoader webClientLoader;

	@Override
	@Cacheable(CacheCustomizer.JSON_RESOURCES_CACHE)
	public Single<HalResponse> loadJsonResource(String uri) {

		return webClientLoader.loadJsonResource(uri).cache();
	}

}
