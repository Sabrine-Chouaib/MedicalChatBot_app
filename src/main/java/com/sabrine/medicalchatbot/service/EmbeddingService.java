package com.sabrine.medicalchatbot.service;

import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

@Service
public class EmbeddingService {

    public float[] embed(String text) {

        JSONObject body = new JSONObject();
        body.put("model", "nomic-embed-text");
        body.put("input", text);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:11434/api/embeddings"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(body.toString()))
                .build();

        try {
            HttpClient client = HttpClient.newHttpClient();
            String response = client.send(request, HttpResponse.BodyHandlers.ofString()).body();

            JSONObject json = new JSONObject(response);
            JSONArray arr = json.getJSONArray("embedding");

            float[] vector = new float[arr.length()];
            for (int i = 0; i < arr.length(); i++)
                vector[i] = arr.getFloat(i);

            return vector;

        } catch (Exception e) {
            throw new RuntimeException("Embedding failed", e);
        }
    }
}
