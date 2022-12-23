package org.example.external.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lombok.RequiredArgsConstructor;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import org.example.cache.ReactorCacheable;
import org.example.dto.PlaceSearchAPIResponse;
import org.example.enums.ServiceExceptionMessages;
import org.example.exception.ExternalAPIException;
import org.example.exception.ServiceException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;

import javax.annotation.PostConstruct;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.Duration;

@Slf4j
@ToString
@Component
@RequiredArgsConstructor
public class KakaoPlaceSearchAPI {
  private final WebClient webClient;
  private final ObjectMapper webClientObjectMapper;
  private boolean dryRun = false;
  @Value("${spring.profiles.active}")
  private String activeProfile;
  @Value("${external.services.kakao.search-keyword-api-endpoint}")
  private String baseUrl;

  @Value("${external.services.kakao.authorization}")
  private String authorization;

  @PostConstruct
  public void init() {
    // HINT : -Dexternal.api.dryRun=true|false
    if (StringUtils.hasText(System.getProperty("external.api.dryRun"))) {
      dryRun = Boolean.parseBoolean(System.getProperty("external.api.dryRun"));
    }
    log.info("+ KakaoPlaceSearchAPI(baseURL={}, authorization={}, dryRun={})", baseUrl, authorization, dryRun);
  }

  @CircuitBreaker(name="externalAPI", fallbackMethod = "fallback")
  public Mono<PlaceSearchAPIResponse> call(String query) {
    if (dryRun) {
      log.warn("Kakao '/v2/local/search/keyword.json' API is set to dry run mode.");
      return Mono.just(new PlaceSearchAPIResponse());
    }
    return webClient.get()
        .uri(baseUrl, uriBuilder -> uriBuilder
            .path("/v2/local/search/keyword.json")
            .queryParam("query", URLEncoder.encode(query, StandardCharsets.UTF_8))
            .queryParam("page", 1)
            .queryParam("size", 10)
            .build())
        .headers(httpHeaders -> httpHeaders.set(HttpHeaders.AUTHORIZATION, authorization))
        .retrieve()
        .onStatus(HttpStatus::is4xxClientError, response -> response.bodyToMono(String.class).map(ExternalAPIException::new))
        .onStatus(HttpStatus::is5xxServerError, response -> response.bodyToMono(String.class).map(ExternalAPIException::new))
        .bodyToMono(PlaceSearchAPIResponse.class)
        .retryWhen(Retry.backoff(3, Duration.ofSeconds(1)).jitter(0d))
        .onErrorResume(e -> {
          log.error("Kakao '/v2/local/search/keyword.json' API ERROR , e : " + e.getMessage());
          return Mono.error(new ServiceException(ServiceExceptionMessages.SERVER_UNKNOWN_ERROR_EXTERNAL_API));
        });
  }

  public Mono<PlaceSearchAPIResponse> fallback(String key, Throwable t) {
    log.error("Kakao '/v2/local/search/keyword.json' API Fallback : " + t.getMessage());
    return Mono.just(new PlaceSearchAPIResponse());
  }
}
