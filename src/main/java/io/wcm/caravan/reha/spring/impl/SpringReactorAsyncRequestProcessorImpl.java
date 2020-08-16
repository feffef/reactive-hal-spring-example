package io.wcm.caravan.reha.spring.impl;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.CacheControl;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.ResponseEntity.BodyBuilder;
import org.springframework.stereotype.Component;
import org.springframework.web.context.annotation.RequestScope;

import com.fasterxml.jackson.databind.JsonNode;

import io.wcm.caravan.reha.api.Reha;
import io.wcm.caravan.reha.api.RehaBuilder;
import io.wcm.caravan.reha.api.common.HalResponse;
import io.wcm.caravan.reha.api.resources.LinkableResource;
import io.wcm.caravan.reha.api.spi.JsonResourceLoader;
import io.wcm.caravan.reha.spring.api.SpringReactorReha;
import io.wcm.caravan.reha.spring.api.SpringRehaAsyncRequestProcessor;
import reactor.core.publisher.Mono;

@Component
@RequestScope
public class SpringReactorAsyncRequestProcessorImpl implements SpringRehaAsyncRequestProcessor {

	private static final Logger log = LoggerFactory.getLogger(SpringReactorAsyncRequestProcessorImpl.class);

	private final URI requestUri;

	private final JsonResourceLoader jsonLoader;

	public SpringReactorAsyncRequestProcessorImpl(@Autowired HttpServletRequest httpRequest,
			@Autowired @Qualifier("cachingJsonResourceLoader") JsonResourceLoader jsonLoader) {

		this.requestUri = getRequestURI(httpRequest);

		this.jsonLoader = jsonLoader;
	}

	@Override
	public <RequestContextType> Mono<ResponseEntity<JsonNode>> processRequest(
			Function<SpringReactorReha, RequestContextType> requestContextConstructor,
			Function<RequestContextType, LinkableResource> resourceImplConstructor) {

		Reha reha = RehaBuilder.withResourceLoader(jsonLoader).buildForRequestTo(requestUri.toString());

		SpringReactorReha springReha = new SpringReactorRehaImpl(reha);

		RequestContextType requestContext = requestContextConstructor.apply(springReha);

		LinkableResource resourceImpl = resourceImplConstructor.apply(requestContext);

		return renderResponse(reha, resourceImpl);
	}

	private static URI getRequestURI(HttpServletRequest httpRequest) {
		String requestUrl = httpRequest.getRequestURL().toString();
		String query = httpRequest.getQueryString();
		if (query != null) {
			requestUrl += "?" + query;
		}
		log.info("Incoming request  | uri={}", requestUrl);
		try {
			return new URI(requestUrl);
		} catch (URISyntaxException ex) {
			throw new RuntimeException("Invalid URL found in request: " + requestUrl, ex);
		}
	}

	private Mono<ResponseEntity<JsonNode>> renderResponse(Reha reha, LinkableResource resourceImpl) {

		CompletionStage<HalResponse> response = reha.renderResponseAsync(resourceImpl);

		CompletionStage<ResponseEntity<JsonNode>> entity = response.thenApply(this::toResponseEntity);

		return Mono.fromCompletionStage(entity);
	}

	private ResponseEntity<JsonNode> toResponseEntity(HalResponse halResponse) {

		BodyBuilder builder = ResponseEntity.status(halResponse.getStatus());

		if (halResponse.getContentType() != null) {
			builder.contentType(MediaType.parseMediaType(halResponse.getContentType()));
		}

		if (halResponse.getMaxAge() != null) {
			builder.cacheControl(CacheControl.maxAge(halResponse.getMaxAge(), TimeUnit.SECONDS));
		}

		log.info("Outgoing response | uri={}", requestUri);

		return builder.body(halResponse.getBody().getModel());
	}

}
