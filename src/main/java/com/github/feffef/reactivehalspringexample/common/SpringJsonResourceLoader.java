package com.github.feffef.reactivehalspringexample.common;

import org.springframework.stereotype.Component;

import io.reactivex.rxjava3.core.Single;
import io.wcm.caravan.hal.microservices.api.client.HalApiDeveloperException;
import io.wcm.caravan.hal.microservices.api.client.JsonResourceLoader;
import io.wcm.caravan.hal.microservices.api.common.HalResponse;

@Component
public class SpringJsonResourceLoader implements JsonResourceLoader {

	@Override
	public Single<HalResponse> loadJsonResource(String uri) {
		return Single.error(new HalApiDeveloperException("Not implemented yet"));
	}

}
