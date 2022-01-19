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
import io.wcm.caravan.rhyme.api.resources.LinkableResource;
import io.wcm.caravan.rhyme.spring.api.SpringReactorRhyme;
import io.wcm.caravan.rhyme.spring.api.SpringRhymeAsyncRequestProcessor;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/")
public class GlobalEntryPointController {

	@Autowired
	private SpringRhymeAsyncRequestProcessor requestProcessor;

	@GetMapping
	public Mono<ResponseEntity<JsonNode>> getEntryPoint() {

		return requestProcessor.processRequest(RequestContext::new, GlobalEntryPointResourceImpl::new);
	}

	class RequestContext {

		private final SpringReactorRhyme rhyme;

		public RequestContext(SpringReactorRhyme rhyme) {
			this.rhyme = rhyme;
		}

	}

	class GlobalEntryPointResourceImpl implements GlobalEntryPointResource {

		private final RequestContext request;

		GlobalEntryPointResourceImpl(RequestContext request) {
			this.request = request;
		}

		@Override
		public List<Link> getSearchServiceEntryPoints() {

			return ImmutableList.of(
					linkTo(FirstSearchController.class, ctrl -> ctrl.getEntryPoint(""),
							"The first example search service, which returns results after a configurable delay"),
					linkTo(SecondSearchController.class, ctrl -> ctrl.getEntryPoint(""),
							"The second example search service, which returns results after a fixed delay"),
					linkTo(GoogleSearchController.class, ctrl -> ctrl.getEntryPoint(""),
							"A service fetching wikipedia results from a Google custom search engine"));
		}

		@Override
		public Link getMetaSeachEntryPoint() {

			return linkTo(MetaSearchController.class, MetaSearchController::getEntryPoint,
					"A meta search service that executes parallel requests to the individual search services, and merges the results");
		}

		@Override
		public List<Link> getPerformanceTestsEntryPoints() {

			return ImmutableList
					.of(linkTo(ConcurrentPerformanceController.class, ConcurrentPerformanceController::getEntryPoint,
							"Performance test that executes a configurable number of simultaneous requests to the "
									+ MetaSearchController.BASE_PATH + " service"));
		}

		@Override
		public Link createLink() {

			Link link = request.rhyme.createLinkTo(GlobalEntryPointController.class,
					GlobalEntryPointController::getEntryPoint);

			link.setTitle("The global entry point which links to all available services on this instance");

			return link;
		}

		private <ControllerType> Link linkTo(Class<? extends ControllerType> controllerClass,
				Function<ControllerType, Mono<ResponseEntity<JsonNode>>> controllerCall, String title) {

			return request.rhyme.createLinkTo(controllerClass, controllerCall);
		}
	}
}
