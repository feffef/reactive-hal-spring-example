package com.github.feffef.reactivehalspringexample.common.resources;

import org.springframework.http.MediaType;

import com.github.feffef.reactivehalspringexample.api.search.ExternalHtmlResource;

import io.wcm.caravan.hal.resource.Link;

public class ExternalHtmlResourceImpl implements ExternalHtmlResource {

	private final String url;
	private final String title;

	public ExternalHtmlResourceImpl(String url, String title) {
		this.url = url;
		this.title = title;
	}

	@Override
	public Link createLink() {

		return new Link(url).setType(MediaType.TEXT_HTML_VALUE).setTitle(title);
	}

}
