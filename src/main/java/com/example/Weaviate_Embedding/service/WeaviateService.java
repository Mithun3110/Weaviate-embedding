package com.example.Weaviate_Embedding.service;

import com.example.Weaviate_Embedding.model.EmbeddedObject;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.*;

@Service
public class WeaviateService {

    private final RestTemplate rest = new RestTemplate();
    private final ObjectMapper mapper = new ObjectMapper();

    private static final String WEAVIATE_URL = "http://localhost:8080/v1/objects";
    private static final String GRAPHQL_URL = "http://localhost:8080/v1/graphql";
    private static final String MISTRAL_URL = "http://localhost:11434/api/chat";

    public ResponseEntity<String> store(EmbeddedObject obj) {
        try {
            Map<String, Object> payload = new HashMap<>();
            payload.put("class", obj.getClassName());
            payload.put("properties", obj.getProperties());
            payload.put("vector", obj.getVector());

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<String> request = new HttpEntity<>(mapper.writeValueAsString(payload), headers);

            return rest.exchange(WEAVIATE_URL, HttpMethod.POST, request, String.class);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Error storing: " + e.getMessage());
        }
    }

    public ResponseEntity<String> search(String className, List<Float> vector, int limit) {
        try {
            String gql = String.format("""
                {
                  Get {
                    %s(nearVector: {
                      vector: %s
                    }, limit: %d) {
                      title
                      content
                    }
                  }
                }
                """, className, mapper.writeValueAsString(vector), limit);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            Map<String, String> gqlBody = Map.of("query", gql);

            HttpEntity<String> request = new HttpEntity<>(mapper.writeValueAsString(gqlBody), headers);
            return rest.postForEntity(GRAPHQL_URL, request, String.class);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Search failed: " + e.getMessage());
        }
    }

    public ResponseEntity<String> askMistral(String question, String className, List<Float> vector) {
        try {
            ResponseEntity<String> searchResponse = search(className, vector, 1);
            String context = mapper.readTree(searchResponse.getBody())
                    .path("data").path("Get").path(className).get(0).toString();

            Map<String, Object> mistralBody = Map.of(
                    "model", "mistral",
                    "messages", List.of(
                            Map.of("role", "system", "content", "Use the following context to answer the question."),
                            Map.of("role", "user", "content", question + "\n\nContext:\n" + context)
                    )
            );

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<String> request = new HttpEntity<>(mapper.writeValueAsString(mistralBody), headers);

            return rest.postForEntity(MISTRAL_URL, request, String.class);

        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Mistral error: " + e.getMessage());
        }
    }
}