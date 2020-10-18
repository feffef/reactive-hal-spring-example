package io.wcm.caravan.rhyme.spring.impl;

import static io.wcm.caravan.rhyme.spring.impl.CacheControlUtil.parseMaxAge;
import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

public class CacheControlUtilTest {

	@Test
	public void should_parse_max_age_if_its_the_only_directive() throws Exception {

		assertThat(parseMaxAge("max-age=123")).isEqualTo(123);
	}

	@Test
	public void should_parse_max_age_if_its_the_first_directive() throws Exception {

		assertThat(parseMaxAge("max-age=123, public")).isEqualTo(123);
	}

	@Test
	public void should_parse_max_age_if_its_the_second_directive() throws Exception {

		assertThat(parseMaxAge("public, max-age=123")).isEqualTo(123);
	}
}
