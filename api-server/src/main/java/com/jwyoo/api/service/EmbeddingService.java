package com.jwyoo.api.service;

import com.pgvector.PGvector;
import com.theokanning.openai.embedding.EmbeddingRequest;
import com.theokanning.openai.service.OpenAiService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.List;

@Slf4j
@Service
public class EmbeddingService {

    private final OpenAiService openAiService;
    private final String embeddingModel;

    public EmbeddingService(
            @Value("${openai.api-key:}") String apiKey,
            @Value("${openai.embedding-model:text-embedding-ada-002}") String embeddingModel
    ) {
        if (apiKey == null || apiKey.isBlank()) {
            log.warn("OpenAI API key not configured");
            this.openAiService = null;
        } else {
            this.openAiService = new OpenAiService(apiKey, Duration.ofSeconds(30));
        }
        this.embeddingModel = embeddingModel;
    }

    public PGvector createEmbedding(String text) {
        if (text == null || text.isBlank()) {
            return createDummyVector();
        }

        if (openAiService == null) {
            return createDummyVector();
        }

        try {
            EmbeddingRequest request = EmbeddingRequest.builder()
                    .model(embeddingModel)
                    .input(List.of(text))
                    .build();

            var result = openAiService.createEmbeddings(request);
            List<Double> embedding = result.getData().get(0).getEmbedding();

            float[] primitiveArray = new float[embedding.size()];
            for (int i = 0; i < embedding.size(); i++) {
                primitiveArray[i] = embedding.get(i).floatValue();
            }

            return new PGvector(primitiveArray);
        } catch (Exception e) {
            log.error("Failed to create embedding", e);
            return createDummyVector();
        }
    }

    private PGvector createDummyVector() {
        float[] array = new float[1536];
        for (int i = 0; i < array.length; i++) {
            array[i] = (float) (Math.random() * 0.01);
        }
        return new PGvector(array);
    }

    public String vectorToString(PGvector vector) {
        return vector == null ? null : vector.toString();
    }
}
