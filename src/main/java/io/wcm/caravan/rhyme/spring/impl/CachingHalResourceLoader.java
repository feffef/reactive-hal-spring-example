package io.wcm.caravan.rhyme.spring.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;

import io.reactivex.rxjava3.core.Single;
import io.wcm.caravan.rhyme.api.common.HalResponse;
import io.wcm.caravan.rhyme.api.spi.HalResourceLoader;

@Component
public class CachingHalResourceLoader implements HalResourceLoader {

	@Autowired
	private WebClientResourceLoader webClientLoader;

	@Override
	@Cacheable("CachingHalResourceLoader")
	public Single<HalResponse> getHalResource(String uri) {

		return webClientLoader.getHalResource(uri).cache();
	}

}
