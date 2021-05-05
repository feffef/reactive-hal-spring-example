package com.github.feffef.reactivehalspringexample.common.context;

import java.time.Duration;
import java.util.Optional;
import java.util.function.Function;

import org.apache.commons.lang3.StringUtils;
import org.springframework.http.ResponseEntity;
import org.springframework.web.util.UriComponentsBuilder;

import com.fasterxml.jackson.databind.JsonNode;

import io.wcm.caravan.hal.resource.Link;
import io.wcm.caravan.rhyme.spring.api.SpringReactorRhyme;
import reactor.core.publisher.Mono;

public abstract class AbstractExampleRequestContext<ControllerType> {

	public static final String QUERY_TIMESTAMP_PARAM = "queryTimestamp";

	private final SpringReactorRhyme rhyme;

	private final Class<? extends ControllerType> controllerClass;

	private Optional<String> queryTimeStamp;

	public AbstractExampleRequestContext(SpringReactorRhyme rhyme, Class<? extends ControllerType> controllerClass) {
		this.rhyme = rhyme;
		this.controllerClass = controllerClass;

		this.queryTimeStamp = getQueryTimestampFromRequestUrl();

		rhyme.setResponseMaxAge(getCacheDuration());
	}

	private Duration getCacheDuration() {

		if (getQueryTimestamp().isPresent()) {
			return Duration.ofDays(365);
		}

		return Duration.ofSeconds(60);
	}

	public <T> T getEntryPoint(String uri, Class<T> halApiInterface) {

		return rhyme.getRemoteResource(uri, halApiInterface);
	}

	public void setResponseMaxAge(Duration duration) {

		rhyme.setResponseMaxAge(duration);
	}

	public Link createLinkTo(Function<ControllerType, Mono<ResponseEntity<JsonNode>>> controllerCall) {

		Link link = rhyme.createLinkTo(controllerClass, controllerCall);

		forwardQueryTimestampIfPresentInRequestUrl(link);

		return link;
	}

	protected void ensureThatQueryTimestampIsPresent() {

		if (!queryTimeStamp.isPresent()) {
			long currentMillis = System.currentTimeMillis();
			long roundedTimestamp = currentMillis - currentMillis % Duration.ofMinutes(10).toMillis();
			queryTimeStamp = Optional.of(Long.toString(roundedTimestamp));
		}
	}

	private Optional<String> getQueryTimestampFromRequestUrl() {

		String timestamp = rhyme.getRequest().getParameter(QUERY_TIMESTAMP_PARAM);

		if (StringUtils.isBlank(timestamp)) {
			return Optional.empty();
		}

		return Optional.of(timestamp);
	}

	public Optional<String> getQueryTimestamp() {
		return queryTimeStamp;
	}

	private void forwardQueryTimestampIfPresentInRequestUrl(Link link) {

		String href = link.getHref();
		if (queryTimeStamp.isPresent() && !href.contains(QUERY_TIMESTAMP_PARAM + "=")) {

			UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(href);
			builder.queryParam(QUERY_TIMESTAMP_PARAM, queryTimeStamp.get());
			href = builder.build().toString();

			link.setHref(href);
		}
	}
}
