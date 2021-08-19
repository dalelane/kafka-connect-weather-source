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

import java.io.IOException;
import java.net.URISyntaxException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.ObjectMapper;

import okhttp3.Authenticator;
import okhttp3.Call;
import okhttp3.Credentials;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.Route;

public class WeatherClient {

    private static final Logger LOG = LoggerFactory.getLogger(WeatherClient.class);

    private final OkHttpClient client;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final String url;
    private final String units;
    private final String apikey;

    public WeatherClient(String url, String apikey, String units) {
        this.url = url;
        this.apikey = apikey;
        this.client = new OkHttpClient.Builder().build();
        this.units = units;
    }

    public WeatherData getWeatherData(LocationCoordinates locationCoordinates) throws Exception {
        HttpUrl url = getCurrentWeatherURL(locationCoordinates);
        System.out.println(url.toString());
        try (Response response = query(url)) {
            if (response.isSuccessful()) {
                String body = response.body().string();
                LOG.debug("getWeatherData response body: {}", body);
                WeatherData.Observation observation = objectMapper.readValue(body, WeatherData.Observation.class);
                return new WeatherData(observation);
            } else {
                throw new Exception("Failed querying Weather for " + locationCoordinates + ". Response: " + response);
            }
        }
    }

    private String getLatLonString(LocationCoordinates locationCoordinates) {
        return String.valueOf(locationCoordinates.latitude()) +
               "," +
               String.valueOf(locationCoordinates.longitude());
    }

    private HttpUrl getCurrentWeatherURL(LocationCoordinates locationCoordinates) throws URISyntaxException {
        return HttpUrl.parse(url).newBuilder().addPathSegments("observations/current")
                .addQueryParameter("geocode", getLatLonString(locationCoordinates))
                .addQueryParameter("language", "en-US")
                .addQueryParameter("apiKey", apikey)
                .addQueryParameter("format", "json")
                .addQueryParameter("units", units).build();
    }

    private Response query(HttpUrl url) throws IOException {
        Request.Builder requestBuilder = new Request.Builder();
        LOG.debug("Querying: {}", url);
        Request request = requestBuilder.url(url).build();
        Call call = client.newCall(request);
        Response response = call.execute();
        LOG.debug("Response: {}", response);
        return response;
    }

}
