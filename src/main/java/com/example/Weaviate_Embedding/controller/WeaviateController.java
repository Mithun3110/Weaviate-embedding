package com.example.Weaviate_Embedding.controller;

import com.example.Weaviate_Embedding.model.EmbeddedObject;
import com.example.Weaviate_Embedding.service.WeaviateService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/weaviate")
public class WeaviateController {

    @Autowired
    private WeaviateService service;

    @PostMapping("/add")
    public ResponseEntity<?> add(@RequestBody EmbeddedObject obj) {
        return service.store(obj);
    }

    @PostMapping("/search")
    public ResponseEntity<?> search(@RequestBody Map<String, Object> body) {
        return service.search((String) body.get("className"), (List<Float>) body.get("vector"), (int) body.get("limit"));
    }

    @PostMapping("/ask-mistral")
    public ResponseEntity<?> askMistral(@RequestBody Map<String, Object> body) {
        return service.askMistral((String) body.get("question"), (String) body.get("className"), (List<Float>) body.get("vector"));
    }
}