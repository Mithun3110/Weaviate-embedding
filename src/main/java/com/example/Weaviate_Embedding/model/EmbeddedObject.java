package com.example.Weaviate_Embedding.model;

import lombok.Data;
import java.util.List;
import java.util.Map;

@Data
public class EmbeddedObject {
    private String className;
    private Map<String, Object> properties;
    private List<Float> vector;
}