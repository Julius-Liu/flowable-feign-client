/**
 * Copyright 2019 Pleo Soft d.o.o. (pleosoft.com)

 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0

 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.pleosoft.feign.flowable;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.ObjectFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.http.HttpMessageConverters;
import org.springframework.cloud.openfeign.support.ResponseEntityDecoder;
import org.springframework.cloud.openfeign.support.SpringDecoder;
import org.springframework.cloud.openfeign.support.SpringEncoder;
import org.springframework.context.annotation.Bean;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;

import feign.RequestInterceptor;
import feign.Retryer;
import feign.codec.Decoder;
import feign.codec.Encoder;
import feign.codec.ErrorDecoder;

public class FlowableFeignConfiguration {

	@Autowired
	private FlowableConfigurationProperties properties;

	@Bean
	public Retryer retryer() {
		return new Retryer.Default();
	}

	@Bean
	public ErrorDecoder errorDecoder() {
		return new ErrorDecoder.Default();
	}

	@Bean
	public Decoder feignDecoder() {
		return new ResponseEntityDecoder(new SpringDecoder(new ObjectFactory<HttpMessageConverters>() {

			@Override
			public HttpMessageConverters getObject() throws BeansException {
				return httpMessageConverters();
			}
		}));
	}

	@Bean
	public Encoder feignEncoder() {
		return new SpringEncoder(new ObjectFactory<HttpMessageConverters>() {

			@Override
			public HttpMessageConverters getObject() throws BeansException {
				return httpMessageConverters();
			}
		});
	}

	@Bean
	public HttpMessageConverters httpMessageConverters() {
		final HttpMessageConverter<?> jacksonConverter = new MappingJackson2HttpMessageConverter(objectMapper());

		return new HttpMessageConverters(jacksonConverter);
	}

	@Bean
	public ObjectMapper objectMapper() {
		final ObjectMapper objectMapper = new ObjectMapper();
		objectMapper.setSerializationInclusion(Include.NON_NULL);
		objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
		final SimpleModule module = new SimpleModule();

		objectMapper.registerModule(module);
		return objectMapper;
	}

	@Bean
	public RequestInterceptor headerAuthRequestInterceptor() {
		return new AuthorizationRequestInterceptor(properties.getAuthorizationHeaderName(),
				properties.getDefaultAuthorization());
	}
}
