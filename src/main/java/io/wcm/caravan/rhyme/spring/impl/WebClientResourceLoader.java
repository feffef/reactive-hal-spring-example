package io.wcm.caravan.rhyme.spring.impl;

import java.net.URI;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.UnsupportedMediaTypeException;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClient.ResponseSpec;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import com.fasterxml.jackson.databind.JsonNode;

import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.schedulers.Schedulers;
import io.wcm.caravan.rhyme.api.common.HalResponse;
import io.wcm.caravan.rhyme.api.exceptions.HalApiClientException;
import io.wcm.caravan.rhyme.api.spi.HalResourceLoader;
import reactor.core.publisher.Mono;
import reactor.netty.http.client.HttpClient;
import reactor.netty.resources.ConnectionProvider;

@Component
public class WebClientResourceLoader implements HalResourceLoader {

	private static final Logger log = LoggerFactory.getLogger(WebClientResourceLoader.class);

	private final ConnectionProvider connectionProvider = ConnectionProvider
			.builder(WebClientResourceLoader.class.getSimpleName()).maxConnections(5000).build();

	@Override
	public Single<HalResponse> getHalResource(String uri) {

		if (log.isDebugEnabled()) {
			log.debug("Fetching resource from " + uri);
		}

		HttpClient httpClient = HttpClient.create(connectionProvider);

		WebClient client = WebClient.builder().clientConnector(new ReactorClientHttpConnector(httpClient)).build();

		ResponseSpec response = client.get().uri(URI.create(uri)).retrieve();

		Mono<HalResponse> halResponse = response.toEntity(JsonNode.class).onErrorMap(ex -> remapException(ex, uri))
				.map(WebClientResourceLoader::toHalResponse);

		return Single.fromCompletionStage(halResponse.toFuture()).observeOn(Schedulers.computation());
	}

	// FIXME: move to util class
	public static HalResponse toHalResponse(ResponseEntity<JsonNode> responseEntity) {

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
			response = response.withMaxAge(maxAge);
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
