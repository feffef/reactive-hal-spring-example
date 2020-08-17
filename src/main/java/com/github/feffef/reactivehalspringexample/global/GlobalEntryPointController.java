package com.github.feffef.reactivehalspringexample.global;

import java.net.URI;
import java.util.List;
import java.util.function.Function;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.databind.JsonNode;
import com.github.feffef.reactivehalspringexample.api.global.GlobalEntryPointResource;
import com.github.feffef.reactivehalspringexample.services.examplesearch.first.FirstSearchController;
import com.github.feffef.reactivehalspringexample.services.examplesearch.second.SecondSearchController;
import com.github.feffef.reactivehalspringexample.services.googlesearch.GoogleSearchController;
import com.github.feffef.reactivehalspringexample.services.metasearch.MetaSearchController;
import com.github.feffef.reactivehalspringexample.services.performance.concurrent.ConcurrentPerformanceController;
import com.google.common.collect.ImmutableList;

import io.wcm.caravan.hal.resource.Link;
import io.wcm.caravan.reha.api.resources.LinkableResource;
import io.wcm.caravan.reha.spring.api.SpringReactorReha;
import io.wcm.caravan.reha.spring.api.SpringRehaAsyncRequestProcessor;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/")
public class GlobalEntryPointController {

	@Autowired
	private SpringRehaAsyncRequestProcessor requestProcessor;

	@GetMapping
	public Mono<ResponseEntity<JsonNode>> getEntryPoint() {

		return requestProcessor.processRequest(RequestContext::new, GlobalEntryPointResourceImpl::new);
	}

	class RequestContext {

		private final SpringReactorReha reha;

		public RequestContext(SpringReactorReha reha) {
			this.reha = reha;
		}

	}

	class GlobalEntryPointResourceImpl implements GlobalEntryPointResource {

		private final RequestContext request;

		GlobalEntryPointResourceImpl(RequestContext request) {
			this.request = request;
		}

		@Override
		public List<LinkableResource> getSearchServiceEntryPoints() {

			return ImmutableList.of(
					linkTo(FirstSearchController.class, ctrl -> ctrl.getEntryPoint(""),
							"The first example search service, which returns results after a configurable delay"),
					linkTo(SecondSearchController.class, ctrl -> ctrl.getEntryPoint(""),
							"The second example search service, which returns results after a fixed delay"),
					linkTo(GoogleSearchController.class, ctrl -> ctrl.getEntryPoint(""),
							"A service fetching wikipedia results from a Google custom search engine"));
		}

		@Override
		public LinkableResource getMetaSeachEntryPoint() {

			return linkTo(MetaSearchController.class, MetaSearchController::getEntryPoint,
					"A meta search service that executes parallel requests to the individual search services, and merges the results");
		}

		@Override
		public List<LinkableResource> getPerformanceTestsEntryPoints() {

			return ImmutableList
					.of(linkTo(ConcurrentPerformanceController.class, ConcurrentPerformanceController::getEntryPoint,
							"Performance test that executes a configurable number of simultaneous requests to the "
									+ MetaSearchController.BASE_PATH + " service"));
		}

		@Override
		public Link createLink() {

			Link link = request.reha.createLinkTo(GlobalEntryPointController.class,
					GlobalEntryPointController::getEntryPoint);

			link.setTitle("The global entry point which links to all available services on this instance");

			return link;
		}

		private <ControllerType> LinkableResource linkTo(Class<? extends ControllerType> controllerClass,
				Function<ControllerType, Mono<ResponseEntity<JsonNode>>> controllerCall, String title) {

			Link link = request.reha.createLinkTo(controllerClass, controllerCall);
			return new LinkableResource() {

				@Override
				public Link createLink() {

					String path = URI.create(link.getHref()).getPath();

					return link.setName(path).setTitle(title);
				}
			};
		}
	}
}
