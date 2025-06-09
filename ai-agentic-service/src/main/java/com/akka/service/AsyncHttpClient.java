package com.akka.service;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class AsyncHttpClient {

    public <T> CompletableFuture<List<T>> getListT(String url, Class<T> responseType) {
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .GET()
                .build();

        return client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(response -> {
                    try {
                        ObjectMapper objectMapper = new ObjectMapper();
                        return objectMapper.readValue(response.body(), objectMapper.getTypeFactory().constructCollectionType(List.class, responseType));
                    } catch (Exception e) {
                        throw new RuntimeException("Failed to deserialize response", e);
                    }
                });
    }

    public <T> CompletableFuture<T> getData(String url, Class<T> responseType) {
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .GET()
                .build();

        return client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(response -> {
                    try {
                        ObjectMapper objectMapper = new ObjectMapper();
                        return objectMapper.readValue(response.body(), responseType);
                    } catch (Exception e) {
                        throw new RuntimeException("Failed to deserialize response", e);
                    }
                });
    }

    public <T> CompletableFuture<T> postAsync(String url, Object requestBody, Class<T> responseType) {
        HttpClient client = HttpClient.newHttpClient();
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            String requestBodyJson = objectMapper.writeValueAsString(requestBody);
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .POST(HttpRequest.BodyPublishers.ofString(requestBodyJson))
                    .header("Content-Type", "application/json")
                    .build();
            return client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                    .thenApply(response -> {
                        try {
                            return objectMapper.readValue(response.body(), responseType);
                        } catch (Exception e) {
                            throw new RuntimeException("Failed to deserialize response", e);
                        }
                    });
        } catch (Exception e) {
            throw new RuntimeException("Failed to serialize request body", e);
        }
    }

}

