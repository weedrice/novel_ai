package com.jwyoo.api.service;

import com.jwyoo.api.entity.RagVector;
import com.jwyoo.api.repository.RagVectorRepository;
import com.pgvector.PGvector;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RagVectorService {

    private final RagVectorRepository ragVectorRepository;
    private final EmbeddingService embeddingService;

    @Transactional
    public RagVector saveEmbedding(String sourceType, Long sourceId, String textChunk, String metadata) {
        log.info("Saving embedding for {}:{}", sourceType, sourceId);

        Optional<RagVector> existing = ragVectorRepository.findBySourceTypeAndSourceId(sourceType, sourceId);
        if (existing.isPresent()) {
            log.info("Updating existing embedding for {}:{}", sourceType, sourceId);
            RagVector ragVector = existing.get();
            ragVector.setTextChunk(textChunk);
            ragVector.setEmbedding(embeddingService.createEmbedding(textChunk));
            ragVector.setMetadata(metadata);
            return ragVectorRepository.save(ragVector);
        }

        PGvector embedding = embeddingService.createEmbedding(textChunk);

        RagVector ragVector = RagVector.builder()
                .sourceType(sourceType)
                .sourceId(sourceId)
                .textChunk(textChunk)
                .embedding(embedding)
                .metadata(metadata)
                .build();

        return ragVectorRepository.save(ragVector);
    }

    @Transactional
    public void deleteEmbedding(String sourceType, Long sourceId) {
        log.info("Deleting embedding for {}:{}", sourceType, sourceId);
        ragVectorRepository.deleteBySourceTypeAndSourceId(sourceType, sourceId);
    }

    public List<RagVector> searchSimilar(String queryText, int limit) {
        log.info("Searching similar vectors for query: {}", queryText.substring(0, Math.min(50, queryText.length())));

        PGvector queryEmbedding = embeddingService.createEmbedding(queryText);
        String embeddingString = embeddingService.vectorToString(queryEmbedding);

        List<Object[]> results = ragVectorRepository.findSimilar(embeddingString, limit);

        return results.stream()
                .map(row -> (RagVector) row[0])
                .toList();
    }

    public List<RagVector> searchSimilarByType(String queryText, String sourceType, int limit) {
        log.info("Searching similar vectors of type {} for query: {}", sourceType, queryText);

        PGvector queryEmbedding = embeddingService.createEmbedding(queryText);
        String embeddingString = embeddingService.vectorToString(queryEmbedding);

        List<Object[]> results = ragVectorRepository.findSimilarBySourceType(embeddingString, sourceType, limit);

        return results.stream()
                .map(row -> (RagVector) row[0])
                .toList();
    }

    public List<RagVector> hybridSearch(String queryText, String keyword, int limit) {
        log.info("Hybrid search with query: {} and keyword: {}", queryText, keyword);

        PGvector queryEmbedding = embeddingService.createEmbedding(queryText);
        String embeddingString = embeddingService.vectorToString(queryEmbedding);

        List<Object[]> results = ragVectorRepository.findByHybridSearch(embeddingString, keyword, limit);

        return results.stream()
                .map(row -> (RagVector) row[0])
                .toList();
    }
}
