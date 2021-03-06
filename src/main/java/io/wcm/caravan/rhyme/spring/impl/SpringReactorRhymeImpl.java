package io.wcm.caravan.rhyme.spring.impl;

import java.time.Duration;
import java.util.function.Function;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.StringUtils;
import org.springframework.core.env.Environment;
import org.springframework.hateoas.server.mvc.WebMvcLinkBuilder;
import org.springframework.http.ResponseEntity;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.util.UriComponentsBuilder;

import com.fasterxml.jackson.databind.JsonNode;

import io.wcm.caravan.hal.resource.Link;
import io.wcm.caravan.rhyme.api.Rhyme;
import io.wcm.caravan.rhyme.spring.api.SpringReactorRhyme;
import reactor.core.publisher.Mono;

final class SpringReactorRhymeImpl implements SpringReactorRhyme {

	private final Rhyme rhyme;
	private final ServletWebRequest webRequest;
	private final Environment environment;

	SpringReactorRhymeImpl(Rhyme rhyme, ServletWebRequest webRequest, Environment environment) {
		this.rhyme = rhyme;
		this.webRequest = webRequest;
		this.environment = environment;
	}

	@Override
	public ServletWebRequest getRequest() {
		return webRequest;
	}

	@Override
	public <T> T getRemoteResource(String uri, Class<T> halApiInterface) {
		return rhyme.getRemoteResource(uri, halApiInterface);
	}

	@Override
	public void setResponseMaxAge(Duration duration) {
		rhyme.setResponseMaxAge(duration);
	}

	@Override
	public <ControllerType> Link createLinkTo(Class<? extends ControllerType> controllerClass,
			Function<ControllerType, Mono<ResponseEntity<JsonNode>>> controllerDummyCall) {

		String url = createControllerUrlWithLinkBuilder(controllerClass, controllerDummyCall);

		// if this is method isn't called from the thread that accepted the request,
		// WebMvcLinkBuilder won't be able to construct anything but the path.
		if (!url.startsWith("http")) {
			// we'll use knowledge from the ServletWebRequest that we captured in the
			// constructor to always have consistent absolute URLs
			url = convertToExternalizedAbsoluteUrl(url);
		}

		return new Link(url);
	}

	private <ControllerType> String createControllerUrlWithLinkBuilder(Class<? extends ControllerType> controllerClass,
			Function<ControllerType, Mono<ResponseEntity<JsonNode>>> controllerDummyCall) {

		ControllerType controllerDummy = WebMvcLinkBuilder.methodOn(controllerClass);

		Mono<ResponseEntity<JsonNode>> invocationResult = controllerDummyCall.apply(controllerDummy);

		return WebMvcLinkBuilder.linkTo(invocationResult).toString();
	}

	private String convertToExternalizedAbsoluteUrl(String relativeUrl) {

		HttpServletRequest servletRequest = webRequest.getRequest();

		UriComponentsBuilder builder = UriComponentsBuilder.newInstance();
		builder.scheme(servletRequest.getScheme());
		builder.host(webRequest.getRequest().getServerName());
		builder.port(servletRequest.getLocalPort());
		builder.path(StringUtils.substringBefore(relativeUrl, "?"));
		builder.query(StringUtils.substringAfter(relativeUrl, "?"));

		return builder.build().toString();
	}

	public String createLocalAbsoluteUrl(String pathAndQuery) {

		int localPort = environment.getProperty("server.port", Integer.class, 8080);

		return "http://localhost:" + localPort + pathAndQuery;
	}
}