package com.github.feffef.reactivehalspringexample.common;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.reactivex.rxjava3.core.Single;
import io.wcm.caravan.hal.microservices.api.client.HalApiClientException;
import io.wcm.caravan.hal.microservices.api.client.JsonResourceLoader;
import io.wcm.caravan.hal.microservices.api.common.HalResponse;
import io.wcm.caravan.hal.resource.HalResource;

@Component
public class MockMvcJsonResourceLoader implements JsonResourceLoader {

	private final JsonFactory JSON_FACTORY = new JsonFactory(new ObjectMapper());;

	private final MockMvc mockMvc;

	public MockMvcJsonResourceLoader(@Autowired WebApplicationContext wac) {
		this.mockMvc = MockMvcBuilders.webAppContextSetup(wac).build();
	}

	@Override
	public Single<HalResponse> loadJsonResource(String uri) {
		try {

			MvcResult asyncResult = mockMvc.perform(get(uri)).andExpect(MockMvcResultMatchers.request().asyncStarted())
					.andReturn();

			MvcResult mvcResult = mockMvc.perform(MockMvcRequestBuilders.asyncDispatch(asyncResult))
					.andExpect(status().isOk())
					.andExpect(MockMvcResultMatchers.content().contentType(HalResource.CONTENT_TYPE)).andReturn();

			MockHttpServletResponse mvcResponse = mvcResult.getResponse();

			String jsonString = mvcResponse.getContentAsString();

			assertThat(jsonString).isNotNull();

			JsonNode jsonNode = JSON_FACTORY.createParser(jsonString).readValueAsTree();

			HalResource hal = new HalResource(jsonNode);

			HalResponse response = new HalResponse().withBody(hal).withStatus(mvcResponse.getStatus())
					.withContentType(mvcResponse.getContentType());

			String cacheControl = mvcResponse.getHeader("Cache-Control");
			if (cacheControl != null) {
				response = response.withMaxAge(CacheControlUtil.parseMaxAge(cacheControl));
			}

			return Single.just(response);

		} catch (Exception e) {

			return Single.error(new HalApiClientException("An exception occured", 500, uri, e));
		}
	}
}
