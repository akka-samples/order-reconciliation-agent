package com.akka.service;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;

public class SimpleHttpClient {

    public <T> T get(String url, Class<T> responseType) {
        try {
            URL urlObj = new URL(url);
            HttpURLConnection connection = (HttpURLConnection) urlObj.openConnection();
            connection.setRequestMethod("GET");
            connection.setConnectTimeout(5000);
            connection.setReadTimeout(5000);

            try (BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
                StringBuilder response = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    response.append(line);
                }
                if (responseType == String.class) { return (T) response.toString(); }
                ObjectMapper objectMapper = new ObjectMapper();
                return objectMapper.readValue(response.toString(), responseType);
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to perform GET request", e);
        }
    }

    public <T> List<T> getListT(String url, Class<T> responseType) {
        try {
            URL urlObj = new URL(url);
            HttpURLConnection connection = (HttpURLConnection) urlObj.openConnection();
            connection.setRequestMethod("GET");
            connection.setConnectTimeout(5000);
            connection.setReadTimeout(5000);

            try (BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
                StringBuilder response = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    response.append(line);
                }

                ObjectMapper objectMapper = new ObjectMapper();
                return objectMapper.readValue(response.toString(), objectMapper.getTypeFactory().constructCollectionType(List.class, responseType));
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to perform GET request", e);
        }

    }

    public <T> T post(String url, Object requestBody, Class<T> responseType) {
        try {
            URL urlObj = new URL(url);
            HttpURLConnection connection = (HttpURLConnection) urlObj.openConnection();
            connection.setRequestMethod("POST");
            connection.setConnectTimeout(5000);
            connection.setReadTimeout(5000);
            connection.setDoOutput(true);
            connection.setRequestProperty("Content-Type", "application/json");

            ObjectMapper objectMapper = new ObjectMapper();
            String requestBodyJson = objectMapper.writeValueAsString(requestBody);

            try (OutputStream os = connection.getOutputStream()) {
                byte[] input = requestBodyJson.getBytes("utf-8");
                os.write(input, 0, input.length);
            }

            try (BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
                StringBuilder response = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    response.append(line);
                }

                return objectMapper.readValue(response.toString(), responseType);
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to perform POST request", e);
        }
    }

}

