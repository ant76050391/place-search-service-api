package org.example;

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.IntStream;
import lombok.extern.slf4j.Slf4j;
import org.example.dto.PlaceSearchResult;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.util.StopWatch;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.netty.http.client.HttpClient;
import reactor.util.function.Tuple2;

@Slf4j
public class HamsterLocalLoadTest {
  static AtomicInteger counter = new AtomicInteger(0);

  // HINT : 가급적이면 -Dexternal.api.dryRun=false 로 놓고 테스트 한다.
  // NOTE : count 를 크게 잡으면, naver api 의 경우, {"errorMessage":"Rate limit exceeded. (속도 제한을
  // 초과했습니다.)","errorCode":"012"} 발생. circuit barker 동작 됨.
  public static void main(String[] args) throws InterruptedException, BrokenBarrierException {
    for (int j = 0; j < 1; j++) {
      final int count = 10;

      CyclicBarrier barrier = new CyclicBarrier(count + 1); // 원기옥..
      ExecutorService es = Executors.newFixedThreadPool(count);

      IntStream.range(0, count)
          .forEach(
              value ->
                  es.submit(
                      () -> {
                        int idx = counter.addAndGet(1);
                        try {
                          barrier.await();
                          log.info("Started Thread {}", idx);

                        } catch (InterruptedException | BrokenBarrierException e) {
                          e.printStackTrace();
                        }

                        final String uri = "http://localhost:8080/v1/place?query=곱창";
                        final WebClient webClient = createWebClient();
                        Tuple2<Long, PlaceSearchResult> tuples =
                            webClient
                                .method(HttpMethod.GET)
                                .uri(uri)
                                .contentType(MediaType.APPLICATION_JSON)
                                .retrieve()
                                .bodyToMono(PlaceSearchResult.class)
                                .elapsed()
                                .block();
                        log.info("Elapsed: {} {} / {}", idx, tuples.getT1(), tuples.getT2());
                      }));

      barrier.await();

      StopWatch main = new StopWatch();
      main.start();

      es.shutdown();
      es.awaitTermination(100, TimeUnit.SECONDS);

      main.stop();
      log.info("Total: {}", main.getTotalTimeSeconds());
    }
  }

  private static ExchangeFilterFunction logRequest() {
    return ExchangeFilterFunction.ofRequestProcessor(
        clientRequest -> {
          log.info(
              "Request: {} {} {}",
              clientRequest.method(),
              clientRequest.url(),
              clientRequest.headers());
          return Mono.just(clientRequest);
        });
  }

  private static WebClient createWebClient() {
    final HttpClient httpClient = HttpClient.create();
    return WebClient.builder()
        .clientConnector(new ReactorClientHttpConnector(httpClient))
        .filters(
            exchangeFilterFunctions -> {
              exchangeFilterFunctions.add(logRequest());
            })
        .build();
  }
}
