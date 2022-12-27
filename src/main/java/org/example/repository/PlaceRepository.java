package org.example.repository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.cache.ReactorCacheable;
import org.example.entity.SearchKeyword;
import org.springframework.data.domain.Range;
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

  public Mono<Double> incrementSearchKeywordScore(SearchKeyword searchKeyword) {
    // NOTE : ZINCRBY는 atomic 이다. Redis는 사실상 단일 스레드 이므로 두 개의 스레드가 동시에 Redis에 명령을 보내더라도 Redis는 명령을 직렬
    // 순서로만 실행한다.
    return redisTemplate.opsForZSet().incrementScore("keywords", searchKeyword.getQuery(), 1L);
  }

  @ReactorCacheable(name = "getSearchKeywords")
  public Flux<ZSetOperations.TypedTuple<Object>> getSearchKeywords() {
    return redisTemplate
        .opsForZSet()
        .reverseRangeWithScores(
            "keywords", Range.of(Range.Bound.inclusive(0L), Range.Bound.inclusive(9L)));
  }
}
