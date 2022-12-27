package org.example.filter;

import java.io.ByteArrayOutputStream;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.ServerWebExchangeDecorator;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

@Slf4j
@Component
public class GlobalWebFilter implements WebFilter {
  @Override
  public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
    ByteArrayOutputStream reqBaos = new ByteArrayOutputStream();
    ByteArrayOutputStream resBaos = new ByteArrayOutputStream();

    return chain.filter(decorate(exchange, reqBaos, resBaos));
  }

  private ServerWebExchange decorate(
      ServerWebExchange exchange, ByteArrayOutputStream reqBaos, ByteArrayOutputStream resBaos) {
    ServerHttpRequest request = exchange.getRequest();
    HttpHeaders headers = request.getHeaders();
    ServerHttpRequest mutateHeaderRequest = mutateHeaderRequest(request, headers);
    ServerWebExchange decoratedExchange =
        new ServerWebExchangeDecorator(exchange) {
          @Override
          public ServerHttpRequest getRequest() {
            return new RequestDecorator(mutateHeaderRequest, reqBaos);
          }

          @Override
          public ServerHttpResponse getResponse() {
            return new ResponseDecorator(exchange.getResponse(), resBaos);
          }
        };
    return decoratedExchange;
  }

  /** 요청 헤더에 수정이 필요할 경우 사용 하자 */
  private ServerHttpRequest mutateHeaderRequest(
      ServerHttpRequest request, HttpHeaders httpHeaders) {
    return request
        .mutate()
        .header("clientIP", request.getRemoteAddress().getAddress().getHostAddress())
        .build();
  }
}
