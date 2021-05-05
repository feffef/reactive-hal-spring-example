package io.wcm.caravan.rhyme.spring.impl;

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
import org.springframework.core.env.Environment;
import org.springframework.http.CacheControl;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.ResponseEntity.BodyBuilder;
import org.springframework.stereotype.Component;
import org.springframework.web.context.annotation.RequestScope;
import org.springframework.web.context.request.ServletWebRequest;

import com.fasterxml.jackson.databind.JsonNode;

import io.reactivex.rxjava3.core.Single;
import io.wcm.caravan.rhyme.api.Rhyme;
import io.wcm.caravan.rhyme.api.RhymeBuilder;
import io.wcm.caravan.rhyme.api.common.HalResponse;
import io.wcm.caravan.rhyme.api.resources.LinkableResource;
import io.wcm.caravan.rhyme.api.spi.HalResourceLoader;
import io.wcm.caravan.rhyme.spring.api.SpringReactorRhyme;
import io.wcm.caravan.rhyme.spring.api.SpringRhymeAsyncRequestProcessor;
import reactor.core.publisher.Mono;

@Component
@RequestScope
public class SpringReactorAsyncRequestProcessorImpl implements SpringRhymeAsyncRequestProcessor {

	private static final Logger log = LoggerFactory.getLogger(SpringReactorAsyncRequestProcessorImpl.class);

	private final Environment environment;

	private final URI requestUri;
	private final ServletWebRequest webRequest;

	private final HalResourceLoader resourceLoader;

	public SpringReactorAsyncRequestProcessorImpl(@Autowired Environment environment,
			@Autowired HttpServletRequest httpRequest, @Autowired ServletWebRequest webRequest,
			@Autowired @Qualifier("cachingHalResourceLoader") HalResourceLoader resourceLoader) {

		this.environment = environment;

		this.requestUri = getRequestURI(httpRequest);
		this.webRequest = webRequest;

		this.resourceLoader = resourceLoader;
	}

	@Override
	public <RequestContextType> Mono<ResponseEntity<JsonNode>> processRequest(
			Function<SpringReactorRhyme, RequestContextType> requestContextConstructor,
			Function<RequestContextType, LinkableResource> resourceImplConstructor) {

		Rhyme rhyme = RhymeBuilder.withResourceLoader(resourceLoader).buildForRequestTo(requestUri.toString());

		SpringReactorRhyme springRhyme = new SpringReactorRhymeImpl(rhyme, webRequest, environment);

		RequestContextType requestContext = requestContextConstructor.apply(springRhyme);

		LinkableResource resourceImpl = resourceImplConstructor.apply(requestContext);

		return renderResponse(rhyme, resourceImpl);
	}

	private static URI getRequestURI(HttpServletRequest httpRequest) {
		String requestUrl = httpRequest.getRequestURL().toString();
		String query = httpRequest.getQueryString();
		if (query != null) {
			requestUrl += "?" + query;
		}
		log.debug("Incoming request  | uri={}", requestUrl);
		try {
			return new URI(requestUrl);
		} catch (URISyntaxException ex) {
			throw new RuntimeException("Invalid URL found in request: " + requestUrl, ex);
		}
	}

	private Mono<ResponseEntity<JsonNode>> renderResponse(Rhyme rhyme, LinkableResource resourceImpl) {

		Single<HalResponse> response = rhyme.renderResponse(resourceImpl);
		
		return Mono.from(response.toFlowable())
		    .map(this::toResponseEntity);
	}

	private ResponseEntity<JsonNode> toResponseEntity(HalResponse halResponse) {

		BodyBuilder builder = ResponseEntity.status(halResponse.getStatus());

		if (halResponse.getContentType() != null) {
			builder.contentType(MediaType.parseMediaType(halResponse.getContentType()));
		}

		if (halResponse.getMaxAge() != null) {
			builder.cacheControl(CacheControl.maxAge(halResponse.getMaxAge(), TimeUnit.SECONDS));
		}

		log.debug("Outgoing response | uri={}", requestUri);

		return builder.body(halResponse.getBody().getModel());
	}

}
