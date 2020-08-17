package com.github.feffef.reactivehalspringexample.common.context;

import java.time.Duration;
import java.util.Optional;
import java.util.function.Function;

import org.apache.commons.lang3.StringUtils;
import org.springframework.http.ResponseEntity;
import org.springframework.web.util.UriComponentsBuilder;

import com.fasterxml.jackson.databind.JsonNode;

import io.wcm.caravan.hal.resource.Link;
import io.wcm.caravan.reha.spring.api.SpringReactorReha;
import reactor.core.publisher.Mono;

public abstract class AbstractExampleRequestContext<ControllerType> {

	public static final String MEMENTO_PARAM_NAME = "memento";

	private final SpringReactorReha reha;

	private final Class<? extends ControllerType> controllerClass;

	private Optional<String> memento;

	public AbstractExampleRequestContext(SpringReactorReha reha, Class<? extends ControllerType> controllerClass) {
		this.reha = reha;
		this.controllerClass = controllerClass;

		this.memento = getMementoFromRequestUrl();

		reha.setResponseMaxAge(getCacheDuration());
	}

	private Duration getCacheDuration() {

		if (getMemento().isPresent()) {
			return Duration.ofDays(365);
		}

		return Duration.ofSeconds(60);
	}

	public <T> T getEntryPoint(String uri, Class<T> halApiInterface) {

		return reha.getEntryPoint(uri, halApiInterface);
	}

	public void setResponseMaxAge(Duration duration) {

		reha.setResponseMaxAge(duration);
	}

	public Link createLinkTo(Function<ControllerType, Mono<ResponseEntity<JsonNode>>> controllerCall) {

		Link link = reha.createLinkTo(controllerClass, controllerCall);

		forwardMementoIfPresentInRequestUrl(link);

		return link;
	}

	protected void ensureThatMementoIsPresent() {

		if (!memento.isPresent()) {
			memento = Optional.of(Long.toString(System.currentTimeMillis()));
		}
	}

	private Optional<String> getMementoFromRequestUrl() {

		String memento = reha.getWebRequest().getParameter(MEMENTO_PARAM_NAME);

		if (StringUtils.isBlank(memento)) {
			return Optional.empty();
		}

		return Optional.of(memento);
	}

	public Optional<String> getMemento() {
		return memento;
	}

	private void forwardMementoIfPresentInRequestUrl(Link link) {

		String href = link.getHref();
		if (memento.isPresent() && !href.contains(MEMENTO_PARAM_NAME + "=")) {

			UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(href);
			builder.queryParam(MEMENTO_PARAM_NAME, memento.get());
			href = builder.build().toString();

			link.setHref(href);
		}
	}
}
