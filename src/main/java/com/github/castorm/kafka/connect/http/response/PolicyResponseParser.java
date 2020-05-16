package com.github.castorm.kafka.connect.http.response;

/*-
 * #%L
 * Kafka Connect HTTP Plugin
 * %%
 * Copyright (C) 2020 CastorM
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

import com.github.castorm.kafka.connect.http.model.HttpRecord;
import com.github.castorm.kafka.connect.http.model.HttpResponse;
import com.github.castorm.kafka.connect.http.response.spi.HttpResponseParser;
import com.github.castorm.kafka.connect.http.response.spi.HttpResponsePolicy;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.Map;
import java.util.function.Function;

import static java.util.Collections.emptyList;

@RequiredArgsConstructor
public class PolicyResponseParser implements HttpResponseParser {

    private final Function<Map<String, ?>, PolicyResponseParserConfig> configFactory;

    private HttpResponseParser delegate;

    private HttpResponsePolicy policy;

    public PolicyResponseParser() {
        this(PolicyResponseParserConfig::new);
    }

    @Override
    public void configure(Map<String, ?> settings) {
        PolicyResponseParserConfig config = configFactory.apply(settings);
        delegate = config.getDelegateParser();
        policy = config.getPolicy();
    }

    @Override
    public List<HttpRecord> parse(HttpResponse response) {
        switch (policy.resolve(response)) {
            case PROCESS:
                return delegate.parse(response);
            case SKIP:
                return emptyList();
            case FAIL:
            default:
                throw new IllegalStateException(String.format("Unexpected HttpResponse status code: %s", response.getCode()));
        }
    }
}
