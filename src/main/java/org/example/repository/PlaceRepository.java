package org.example.repository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Range;
import org.springframework.data.redis.connection.RedisZSetCommands;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Slf4j
@Repository
@RequiredArgsConstructor
@SuppressWarnings("Duplicates")
public class PlaceRepository {
  private final ReactiveRedisTemplate<String, Object> redisTemplate;

  public Mono<Double> incrementSearchKeywordScore(String query) {
    return redisTemplate.opsForZSet().incrementScore("keywords", query, 1L);
  }

  public Flux<ZSetOperations.TypedTuple<Object>> getSearchKeywords() {
    return redisTemplate.opsForZSet().reverseRangeWithScores(
        "keywords",
        Range.of(
            Range.Bound.inclusive(0L),
            Range.Bound.inclusive(9L)
        ));
  }
}
