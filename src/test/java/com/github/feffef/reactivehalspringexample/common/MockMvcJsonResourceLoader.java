package com.github.feffef.reactivehalspringexample.common;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.asyncDispatch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.request;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.io.IOException;
import java.io.UnsupportedEncodingException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.reactivex.rxjava3.core.Single;
import io.wcm.caravan.reha.api.client.HalApiClientException;
import io.wcm.caravan.reha.api.client.JsonResourceLoader;
import io.wcm.caravan.reha.api.common.HalResponse;
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
			MockHttpServletResponse mvcResponse = getServletResponse(uri);

			HalResponse halResponse = convertToHalResponse(mvcResponse);

			return Single.just(halResponse);

		} catch (Exception e) {
			return Single.error(new HalApiClientException("An exception occured", 500, uri, e));
		}
	}

	private MockHttpServletResponse getServletResponse(String uri) throws Exception {

		MvcResult asyncResult = mockMvc.perform(get(uri)).andExpect(request().asyncStarted())
				.andReturn();

		MvcResult mvcResult = mockMvc.perform(asyncDispatch(asyncResult))
				.andExpect(status().isOk())
				.andExpect(content().contentType(HalResource.CONTENT_TYPE))
				.andReturn(); 

		return mvcResult.getResponse();
	}

	private HalResource parseHalResource(MockHttpServletResponse mvcResponse)
			throws UnsupportedEncodingException, IOException, JsonParseException {

		String jsonString = mvcResponse.getContentAsString();
		assertThat(jsonString).isNotNull();

		JsonNode jsonNode = JSON_FACTORY.createParser(jsonString).readValueAsTree();

		return new HalResource(jsonNode);
	}

	private HalResponse convertToHalResponse(MockHttpServletResponse mvcResponse)
			throws UnsupportedEncodingException, IOException, JsonParseException {

		HalResource hal = parseHalResource(mvcResponse);

		HalResponse halResponse = new HalResponse()
				.withBody(hal)
				.withStatus(mvcResponse.getStatus())
				.withContentType(mvcResponse.getContentType());

		String cacheControl = mvcResponse.getHeader("Cache-Control");
		if (cacheControl != null) {
			halResponse = halResponse.withMaxAge(CacheControlUtil.parseMaxAge(cacheControl));
		}
		return halResponse;
	}

}
