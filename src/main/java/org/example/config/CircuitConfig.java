package org.example.config;

import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import io.netty.handler.timeout.TimeoutException;
import java.io.IOException;
import java.time.Duration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.HttpClientErrorException;

@Configuration
public class CircuitConfig {

  /**
   * 3가지 상태 전환 과정
   *
   * <p>Closed 상태 특정 모듈이 요청한 결과(성공/실패)를 기록. 실패 횟수(또는 시간)가 임계치에 도달했을시 Open 상태로 전환 Open 상태 지정된 시간만큼
   * open 상태를 유지하며 요청에 대해 지정된 결과를 응답한다.(예외를 던지거나 임시복구 데이터를 응답한다.) Half Open 상태 Open 상태가 종료되면, 조금씩
   * 요청을 받아 처리한다. 요청 결과가 성공적이면 Closed 상태로 전환
   */
  @Bean
  public CircuitBreakerRegistry circuitBreakerRegistry() {
    return CircuitBreakerRegistry.of(
        CircuitBreakerConfig.custom() // 2
            .slidingWindow(10, 5, CircuitBreakerConfig.SlidingWindowType.TIME_BASED)
            .failureRateThreshold(50)
            .recordExceptions(IOException.class, TimeoutException.class)
            .ignoreExceptions(HttpClientErrorException.class)
            .slowCallDurationThreshold(Duration.ofMillis(500))
            .slowCallRateThreshold(50)
            .waitDurationInOpenState(Duration.ofSeconds(10))
            .permittedNumberOfCallsInHalfOpenState(1)
            .maxWaitDurationInHalfOpenState(Duration.ofSeconds(10))
            .build());
  }

  @Bean
  public CircuitBreaker circuitBreaker(CircuitBreakerRegistry circuitBreakerRegistry) {
    return circuitBreakerRegistry.circuitBreaker("externalAPI");
  }
}
