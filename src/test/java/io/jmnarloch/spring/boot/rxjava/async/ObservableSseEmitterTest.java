/**
 * Copyright (c) 2015-2016 the original author or authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.jmnarloch.spring.boot.rxjava.async;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.IntegrationTest;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.boot.test.TestRestTemplate;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import rx.Observable;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * Tests the {@link ObservableSseEmitter} class.
 *
 * @author Jakub Narloch
 */
@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = ObservableSseEmitterTest.Application.class)
@WebAppConfiguration
@IntegrationTest({"server.port=0"})
@DirtiesContext
public class ObservableSseEmitterTest {

    @Value("${local.server.port}")
    private int port = 0;

    private TestRestTemplate restTemplate = new TestRestTemplate();

    @Configuration
    @EnableAutoConfiguration
    @RestController
    protected static class Application {

        @RequestMapping(method = RequestMethod.GET, value = "/sse")
        public ObservableSseEmitter<String> single() {
            return new ObservableSseEmitter<String>(Observable.just("single value"));
        }

        @RequestMapping(method = RequestMethod.GET, value = "/messages")
        public ObservableSseEmitter<String> messages() {
            return new ObservableSseEmitter<String>(Observable.just("message 1", "message 2", "message 3"));
        }
    }

    @Test
    public void shouldRetrieveSse() {

        // when
        ResponseEntity<String> response = restTemplate.getForEntity(path("/sse"), String.class);

        // then
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("data:single value\n\n", response.getBody());
    }

    @Test
    public void shouldRetrieveSseWithMultipleMessages() {

        // when
        ResponseEntity<String> response = restTemplate.getForEntity(path("/messages"), String.class);

        // then
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("data:message 1\n\ndata:message 2\n\ndata:message 3\n\n", response.getBody());
    }

    private String path(String context) {
        return String.format("http://localhost:%d%s", port, context);
    }
}