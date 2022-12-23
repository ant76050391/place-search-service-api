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
import org.springframework.http.MediaType;
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
public class NaverPlaceSearchAPI {
  private final WebClient webClient;
  private final ObjectMapper webClientObjectMapper;
  private boolean dryRun = false;
  @Value("${spring.profiles.active}")
  private String activeProfile;
  @Value("${external.services.naver.search-keyword-api-endpoint}")
  private String baseUrl;

  @Value("${external.services.naver.x-naver-client-id}")
  private String clientId;

  @Value("${external.services.naver.x-naver-client-secret}")
  private String clientSecret;

  @PostConstruct
  public void init() {
    // HINT : -Dexternal.api.dryRun=true|false
    if (StringUtils.hasText(System.getProperty("external.api.dryRun"))) {
      dryRun = Boolean.parseBoolean(System.getProperty("external.api.dryRun"));
    }
    log.info("+ NaverPlaceSearchAPI(baseURL={}, clientId={}, clientSecret={}, dryRun={})", baseUrl, clientId, clientSecret, dryRun);
  }

  @CircuitBreaker(name="externalAPI", fallbackMethod = "fallback")
  public Mono<PlaceSearchAPIResponse> call(String query) {
    if (dryRun) {
      log.warn("Naver '/v1/search/local.json' API is set to dry run mode.");
      return Mono.just(new PlaceSearchAPIResponse());
    }
    return webClient.get()
        .uri(baseUrl, uriBuilder -> uriBuilder
            .path("/v1/search/local.json")
            .queryParam("query", URLEncoder.encode(query, StandardCharsets.UTF_8))
            .queryParam("start", 1)
            .queryParam("display", 10)
            .queryParam("sort", "random")
            .build())
        .headers(httpHeaders -> {
          httpHeaders.set(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);
          httpHeaders.set("x-naver-client-id", clientId);
          httpHeaders.set("x-naver-client-secret", clientSecret);
        })
        .retrieve()
        .onStatus(HttpStatus::is4xxClientError, response -> response.bodyToMono(String.class).map(ExternalAPIException::new))
        .onStatus(HttpStatus::is5xxServerError, response -> response.bodyToMono(String.class).map(ExternalAPIException::new))
        .bodyToMono(PlaceSearchAPIResponse.class)
        .retryWhen(Retry.backoff(3, Duration.ofSeconds(1)).jitter(0d))
        .onErrorResume(e -> {
          log.error("Naver '/v1/search/local.json' API ERROR , e : " + e.getMessage());
          return Mono.error(new ServiceException(ServiceExceptionMessages.SERVER_UNKNOWN_ERROR_EXTERNAL_API));
        });
  }

  public Mono<PlaceSearchAPIResponse> fallback(String key, Throwable t) {
    log.error("Naver '/v1/search/local.json' API Fallback : " + t.getMessage());
    return Mono.just(new PlaceSearchAPIResponse());
  }
}