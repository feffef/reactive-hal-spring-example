package com.github.feffef.reactivehalspringexample.common;

import java.net.URI;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.UnsupportedMediaTypeException;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClient.ResponseSpec;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import com.fasterxml.jackson.databind.JsonNode;

import io.reactivex.rxjava3.core.Single;
import io.wcm.caravan.hal.microservices.api.client.HalApiClientException;
import io.wcm.caravan.hal.microservices.api.client.JsonResourceLoader;
import io.wcm.caravan.hal.microservices.api.common.HalResponse;
import reactor.core.publisher.Mono;

@Component
public class WebClientJsonResourceLoader implements JsonResourceLoader {

	@Override
	public Single<HalResponse> loadJsonResource(String uri) {

		WebClient client = WebClient.create();

		ResponseSpec response = client.get().uri(URI.create(uri)).retrieve();

		Mono<HalResponse> halResponse = response.toEntity(JsonNode.class).onErrorMap(ex -> remapException(ex, uri))
				.map(this::toHalResponse);

		return Single.fromCompletionStage(halResponse.toFuture());
	}

	private HalResponse toHalResponse(ResponseEntity<JsonNode> responseEntity) {

		HalResponse response = new HalResponse().withStatus(responseEntity.getStatusCodeValue());

		if (responseEntity.hasBody()) {
			response = response.withBody(responseEntity.getBody());
		}

		MediaType contentType = responseEntity.getHeaders().getContentType();
		if (contentType != null) {
			response = response.withContentType(contentType.toString());
		}

		String cacheControl = responseEntity.getHeaders().getCacheControl();
		Integer maxAge = CacheControlUtil.parseMaxAge(cacheControl);
		if (maxAge != null) {
			response.withMaxAge(maxAge);
		}

		return response;
	}

	private Throwable remapException(Throwable cause, String requestUrl) {

		String msg;
		Integer statusCode = null;

		if (cause instanceof WebClientResponseException) {
			statusCode = ((WebClientResponseException) cause).getRawStatusCode();
			msg = "Received unexpected status code " + statusCode + " when fetching upstream resource from "
					+ requestUrl;
		} else if (cause instanceof UnsupportedMediaTypeException) {
			msg = "The resource retrieved from " + requestUrl + " is not a HAL+JSON resource";
		} else {
			msg = "Failed to retrieve any response from " + requestUrl;
		}

		return new HalApiClientException(msg, statusCode, requestUrl, cause);
	}

}