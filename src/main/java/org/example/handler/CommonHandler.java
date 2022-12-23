package org.example.handler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.dto.HealthCheck;
import org.example.enums.ServiceExceptionMessages;
import org.example.exception.ServiceException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Mono;

import java.nio.file.Files;
import java.nio.file.Paths;

import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.web.reactive.function.server.ServerResponse.ok;

@Slf4j
@Component
@RequiredArgsConstructor
public class CommonHandler {
  private static final String ALIVE_FILE_PATH;

  static {
    ALIVE_FILE_PATH = System.getProperty("healthcheck.filepath");
  }

  @Value("${git.branch}")
  private String branch;

  public Mono<ServerResponse> healthCheck(ServerRequest serverRequest) {
    if (Files.exists(Paths.get(ALIVE_FILE_PATH))) {
      return ok()
          .contentType(APPLICATION_JSON)
          .body(Mono.just(HealthCheck.builder()
              .releaseVersion(branch)
              .yourIP(serverRequest.headers().firstHeader("clientIP"))
              .isHealthCheck(true)
              .build()), HealthCheck.class)
          .onErrorResume(throwable ->
              Mono.error(new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                  "An unexpected error has occurred." + throwable.getMessage(),
                  throwable))); // NOTE : 예상하지 못한 예러
    } else {
      return Mono.error(new ServiceException(ServiceExceptionMessages.SERVER_SHUTTING_DOWN));
    }
  }

  public Mono<ServerResponse> accessDenied(ServerRequest serverRequest) {
    return Mono.error(new ServiceException(ServiceExceptionMessages.FORBIDDEN_INVALID_REQUIRED_HEADERS));
  }
}
