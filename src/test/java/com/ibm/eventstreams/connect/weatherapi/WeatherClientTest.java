/*
 * Copyright 2019 IBM Corporation
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package com.ibm.eventstreams.connect.weatherapi;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.nio.file.Files;
import java.nio.file.Paths;

import org.junit.Before;
import org.junit.Test;

import com.ibm.eventstreams.connect.weatherapi.LocationCoordinates;
import com.ibm.eventstreams.connect.weatherapi.WeatherClient;
import com.ibm.eventstreams.connect.weatherapi.WeatherData;

import okhttp3.HttpUrl;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;

public class WeatherClientTest {

    private static final LocationCoordinates ATLANTA = new LocationCoordinates("Atlanta", 33.749, -84.39);

    private WeatherClient client;
    private MockWebServer server;
    private HttpUrl url;

    @Before
    public void setUp() throws Exception {
        server = new MockWebServer();
        server.start();
        url = server.url("/");
        client = new WeatherClient(url.toString(), "password", "m");
    }

    @Test
    public void testWeatherServiceFailure() throws Exception {
        server.enqueue(new MockResponse().setResponseCode(500));
        try {
            client.getWeatherData(ATLANTA);
            fail("Client should throw when receiving a 500 response");
        } catch (Exception e) {
            assertTrue(e.getMessage().contains("Atlanta"));
        }
    }

}
