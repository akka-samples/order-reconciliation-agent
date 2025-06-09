package com.external.service;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class SimpleHttpClient {

    private static final SimpleHttpClient simpleHttpClient = new SimpleHttpClient();

    private SimpleHttpClient() {

    }

    public static SimpleHttpClient getInstance() {
        return simpleHttpClient;
    }

    public <T> T get(String url, Class<T> responseType) {
        try {
            // Create a URL object from the string URL
            URL urlObj = new URL(url);

            // Open an HttpURLConnection to the URL
            HttpURLConnection connection = (HttpURLConnection) urlObj.openConnection();
            connection.setRequestMethod("GET");
            connection.setConnectTimeout(5000);
            connection.setReadTimeout(5000);

            // Read the response
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
                StringBuilder response = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    response.append(line);
                }

                // Deserialize the JSON response to the desired responseType
                ObjectMapper objectMapper = new ObjectMapper();
                return objectMapper.readValue(response.toString(), responseType);
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to perform GET request", e);
        }
    }

    public <T> T post(String url, Object requestBody, Class<T> responseType) {
        try {
            // Create a URL object from the string URL
            URL urlObj = new URL(url);
            // Open an HttpURLConnection to the URL
            HttpURLConnection connection = (HttpURLConnection) urlObj.openConnection();
            connection.setRequestMethod("POST");
            connection.setConnectTimeout(5000);
            connection.setReadTimeout(5000);
            connection.setDoOutput(true);
            connection.setRequestProperty("Content-Type", "application/json");

            // Serialize the request body to JSON
            ObjectMapper objectMapper = new ObjectMapper();
            String requestBodyJson = objectMapper.writeValueAsString(requestBody);

            // Write the request body to the connection
            try (OutputStream os = connection.getOutputStream()) {
                byte[] input = requestBodyJson.getBytes("utf-8");
                os.write(input, 0, input.length);
            }

            // Read the response
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
                StringBuilder response = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    response.append(line);
                }

                // Deserialize the JSON response to the desired responseType
                return objectMapper.readValue(response.toString(), responseType);
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to perform POST request", e);
        }
    }

//    public static void main(String[] args) {
//        SimpleHttpClient simpleHttpClient = new SimpleHttpClient();
//        ExceptionReasoningRequest request = new ExceptionReasoningRequest("test");
//        ExceptionReasoningResponse post = simpleHttpClient.post("http://127.0.0.1:9000/api/react-agent", request, ExceptionReasoningResponse.class);
//        System.out.println(post);
//    }
}

