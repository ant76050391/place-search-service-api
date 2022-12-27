package org.example.filter.log;

import static net.logstash.logback.marker.Markers.appendEntries;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.annotation.MergedAnnotation;
import org.springframework.core.annotation.MergedAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

@Slf4j
@Component
@RequiredArgsConstructor
public class LogFilter implements WebFilter {

  @Value("${git.branch}")
  private String branch;

  @Override
  public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
    final String ignorePatterns = ".*(health-check|api-docs|swagger).*";

    if (exchange.getRequest().getURI().getPath().matches(ignorePatterns)) {
      return chain.filter(exchange);
    }

    MediaType contentType = exchange.getRequest().getHeaders().getContentType();

    BodyCaptureExchange bodyCaptureExchange = new BodyCaptureExchange(exchange);

    Map<String, Object> logField = new HashMap<>();
    logField.put("path", bodyCaptureExchange.getRequest().getURI().getPath());
    logField.put("query", bodyCaptureExchange.getRequest().getURI().getQuery());
    logField.put("clientIP", bodyCaptureExchange.getRequest().getHeaders().get("clientIP"));
    logField.put("method", bodyCaptureExchange.getRequest().getMethod());
    logField.put("releaseVersion", branch);
    logField.put("requestHeaders", bodyCaptureExchange.getRequest().getHeaders());

    return chain
        .filter(bodyCaptureExchange)
        .doOnSuccess(
            unused -> {
              logField.put(
                  "requestBody",
                  json2Obj(bodyCaptureExchange.getRequest().getFullBody(), Object.class));
              logField.put(
                  "responseBody",
                  json2Obj(bodyCaptureExchange.getResponse().getFullBody(), Object.class));
              logField.put(
                  "httStatusCode",
                  Objects.requireNonNull(bodyCaptureExchange.getResponse().getStatusCode())
                      .value());
              log.info(appendEntries(logField), "trace log");
            })
        .doOnError(
            throwable -> {
              logField.put(
                  "requestBody",
                  json2Obj(bodyCaptureExchange.getRequest().getFullBody(), Object.class));
              logField.put("responseBody", json2Obj(throwable.toString(), Object.class));
              HttpStatus httpStatus = determineHttpStatus(throwable);
              // NOTE : 실패할 경우 로직에 정의 된 에러 코드로 내려 보내기 위해 상태 코드를 별도로 가져온다.
              logField.put("httStatusCode", httpStatus.value());
              log.info(appendEntries(logField), "trace log");
            });
  }

  private HttpStatus determineHttpStatus(Throwable error) {
    MergedAnnotation<ResponseStatus> responseStatusAnnotation =
        MergedAnnotations.from(error.getClass(), MergedAnnotations.SearchStrategy.TYPE_HIERARCHY)
            .get(ResponseStatus.class);

    if (error instanceof ResponseStatusException) {
      return ((ResponseStatusException) error).getStatus();
    }
    return responseStatusAnnotation
        .getValue("code", HttpStatus.class)
        .orElse(HttpStatus.INTERNAL_SERVER_ERROR);
  }

  public <T> T json2Obj(String json, @NonNull Class<T> c) {
    try {
      if (!StringUtils.hasText(json)) {
        json = "{}";
      }
      return new ObjectMapper().readValue(json, c);
    } catch (Exception e) {
      log.warn(
          "Since the request/response is not in json format, it is returned as a String object. : {}",
          e.getMessage());
      return (T) json;
    }
  }
}
