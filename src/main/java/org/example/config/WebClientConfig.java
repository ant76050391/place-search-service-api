package org.example.config;

import io.github.resilience4j.common.circuitbreaker.configuration.CircuitBreakerConfigCustomizer;
import io.netty.channel.ChannelOption;
import io.netty.handler.timeout.ReadTimeoutHandler;
import io.netty.handler.timeout.WriteTimeoutHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.util.StringUtils;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;
import org.springframework.web.reactive.function.client.ExchangeStrategies;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.netty.http.client.HttpClient;
import reactor.netty.resources.ConnectionProvider;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

import static io.github.resilience4j.circuitbreaker.CircuitBreakerConfig.SlidingWindowType.COUNT_BASED;

@Slf4j
@Configuration
public class WebClientConfig {

  private static final int BUFFER_MAX_MEMORY_SIZE = 1024 * 1024 * 16;
  @Value("${spring.profiles.active}")
  private String activeProfile;

  @Bean
  public CircuitBreakerConfigCustomizer externalAPICircuitBreakerConfig() {
    return CircuitBreakerConfigCustomizer
        .of("externalAPI",
            builder -> builder.slidingWindowSize(10)
                .slidingWindowType(COUNT_BASED)
                .waitDurationInOpenState(Duration.ofSeconds(5))
                .minimumNumberOfCalls(5)
                .failureRateThreshold(50.0f));
  }

  private ExchangeFilterFunction logRequest() {
    return ExchangeFilterFunction.ofRequestProcessor(clientRequest -> {
      if (StringUtils.hasText(activeProfile) &&
          ("local".equalsIgnoreCase(activeProfile) || ("alpha").equalsIgnoreCase(activeProfile))) {
        log.info("Request: {} {} {}", clientRequest.method(), clientRequest.url(), clientRequest.headers());
      }
      return Mono.just(clientRequest);
    });
  }

  @Bean
  public WebClient webClient() {
    HttpClient httpClient = this.createHttpClient();
    return WebClient.builder()
        .clientConnector(new ReactorClientHttpConnector(httpClient))
        .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
        .defaultHeader(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
        .exchangeStrategies(ExchangeStrategies.builder()
            .codecs(configurer -> configurer
                .defaultCodecs()
                .maxInMemorySize(BUFFER_MAX_MEMORY_SIZE))
            .build())
        //.filter(logRequest())
        .build();
  }

  /**
   * 파리미터 설명
   * maxConnections : 유지할 Connection Pool의 수
   * 기본값 : max(프로세서수, 8) * 2
   * 참고로 max 값 많큼 미리 생성해 놓지 않고 필요할때마다 생성한다. 말 그대로 최대 생성가능한 수이다.
   * maxIdleTime : 사용하지 않는 상태(idle)의 Connection이 유지되는 시간. AWS ELB의 기본 idle timeout 값은 60초이다. 그보다 먼저 끊어져야 한다.
   * 기본값 : 무제한 (-1)
   * maxLifeTime : Connection Pool 에서의 최대 수명 시간
   * 기본값 : 무제한 (-1)
   * pendingAcquireTimeout : Connection Pool에서 사용할 수 있는 Connection 이 없을때 (모두 사용중일때) Connection을 얻기 위해 대기하는 시간
   * 기본값 : 45초
   * pendingAcquireMaxCount : Connection을 얻기 위해 대기하는 최대 수
   * 기본값 : 무제한 (-1)
   * evictInBackground : 백그라운드에서 만료된 connection을 제거하는 주기
   * lifo : 마지막에 사용된 커넥션을 재 사용, fifo – 처음 사용된(가장오래된) 커넥션을 재 사용
   * metrics : connection pool 사용 정보를 actuator metric에 노출
   */
  public HttpClient createHttpClient() {
    ConnectionProvider provider = ConnectionProvider.builder("place-holder-http-provider")
        //.maxConnections(100)
        .maxIdleTime(Duration.ofSeconds(58))
        .maxLifeTime(Duration.ofSeconds(58))
        .pendingAcquireTimeout(Duration.ofMillis(5000))
        .pendingAcquireMaxCount(-1)
        .evictInBackground(Duration.ofSeconds(30))
        .lifo()
        .metrics(false)
        .build();
    return HttpClient.create(provider)
        .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 5000)
        .responseTimeout(Duration.ofMillis(5000))
        .doOnConnected(conn -> {
          conn.addHandlerLast(new ReadTimeoutHandler(5000, TimeUnit.MILLISECONDS))
              .addHandlerLast(new WriteTimeoutHandler(5000, TimeUnit.MILLISECONDS));
        });
  }
}
