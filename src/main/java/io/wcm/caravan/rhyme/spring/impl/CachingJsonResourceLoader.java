package io.wcm.caravan.rhyme.spring.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;

import io.reactivex.rxjava3.core.Single;
import io.wcm.caravan.rhyme.api.common.HalResponse;
import io.wcm.caravan.rhyme.api.spi.JsonResourceLoader;

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
