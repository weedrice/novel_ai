package com.jwyoo.api.repository;

import com.jwyoo.api.entity.RagVector;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * RAG Vector Repository
 * 임베딩 벡터 기반 유사도 검색 지원
 */
@Repository
public interface RagVectorRepository extends JpaRepository<RagVector, Long> {

    /**
     * 소스 타입과 ID로 조회
     */
    Optional<RagVector> findBySourceTypeAndSourceId(String sourceType, Long sourceId);

    /**
     * 소스 타입으로 모든 벡터 조회
     */
    List<RagVector> findBySourceType(String sourceType);

    /**
     * 소스 타입과 ID로 삭제
     */
    void deleteBySourceTypeAndSourceId(String sourceType, Long sourceId);

    /**
     * 벡터 유사도 검색 (Cosine Distance)
     *
     * @param embedding 검색할 벡터
     * @param limit 결과 개수
     * @return 유사도 순으로 정렬된 결과 (가장 유사한 것부터)
     */
    @Query(value = """
        SELECT r.*,
               r.embedding <=> CAST(:embedding AS vector) AS distance
        FROM rag_vectors r
        ORDER BY r.embedding <=> CAST(:embedding AS vector)
        LIMIT :limit
        """, nativeQuery = true)
    List<Object[]> findSimilar(@Param("embedding") String embedding, @Param("limit") int limit);

    /**
     * 특정 소스 타입에 대한 벡터 유사도 검색
     *
     * @param embedding 검색할 벡터
     * @param sourceType 소스 타입 필터
     * @param limit 결과 개수
     * @return 유사도 순으로 정렬된 결과
     */
    @Query(value = """
        SELECT r.*,
               r.embedding <=> CAST(:embedding AS vector) AS distance
        FROM rag_vectors r
        WHERE r.source_type = :sourceType
        ORDER BY r.embedding <=> CAST(:embedding AS vector)
        LIMIT :limit
        """, nativeQuery = true)
    List<Object[]> findSimilarBySourceType(
        @Param("embedding") String embedding,
        @Param("sourceType") String sourceType,
        @Param("limit") int limit
    );

    /**
     * 하이브리드 검색: 키워드 + 벡터 유사도
     *
     * @param embedding 검색할 벡터
     * @param keyword 키워드 검색어
     * @param limit 결과 개수
     * @return 하이브리드 검색 결과
     */
    @Query(value = """
        SELECT r.*,
               r.embedding <=> CAST(:embedding AS vector) AS distance,
               ts_rank(to_tsvector('simple', r.text_chunk), plainto_tsquery('simple', :keyword)) AS rank
        FROM rag_vectors r
        WHERE to_tsvector('simple', r.text_chunk) @@ plainto_tsquery('simple', :keyword)
           OR r.text_chunk ILIKE CONCAT('%', :keyword, '%')
        ORDER BY
            (r.embedding <=> CAST(:embedding AS vector)) * 0.7 +
            (1 - ts_rank(to_tsvector('simple', r.text_chunk), plainto_tsquery('simple', :keyword))) * 0.3
        LIMIT :limit
        """, nativeQuery = true)
    List<Object[]> findByHybridSearch(
        @Param("embedding") String embedding,
        @Param("keyword") String keyword,
        @Param("limit") int limit
    );
}
