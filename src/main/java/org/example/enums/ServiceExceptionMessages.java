package org.example.enums;

import java.util.Collections;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.Getter;
import org.springframework.http.HttpStatus;

public enum ServiceExceptionMessages {
  // NOTE : 5XX
  SERVER_CACHING_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "SERVER_CACHING_ERROR"),
  SERVER_UNKNOWN_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "SERVER_UNKNOWN_ERROR"),
  SERVER_SHUTTING_DOWN(HttpStatus.SERVICE_UNAVAILABLE, "SERVER_SHUTTING_DOWN"),
  SERVER_UNKNOWN_ERROR_EXTERNAL_API(
      HttpStatus.INTERNAL_SERVER_ERROR, "SERVER_UNKNOWN_ERROR_EXTERNAL_API"),
  // NOTE : 404
  NOT_FOUND_PLACEMENT_INFO(HttpStatus.NOT_FOUND, "NOT_FOUND_PLACE"),
  // NOTE : 403
  FORBIDDEN_INVALID_REQUIRED_HEADERS(HttpStatus.FORBIDDEN, "FORBIDDEN_INVALID_REQUIRED_HEADERS"),
  // NOTE : 401
  UNAUTHORIZED_EXTERNAL_API(HttpStatus.UNAUTHORIZED, "UNAUTHORIZED_EXTERNAL_API"),
  // NOTE : 400
  BAD_REQUEST_QUERY_PARAMETER_REQUIRED(
      HttpStatus.BAD_REQUEST, "BAD_REQUEST_QUERY_PARAMETER_REQUIRED"),
  // NOTE : 2XX
  NO_CONTENT(
      HttpStatus.NO_CONTENT,
      "This response does not display the body. So, if possible, 200 (OK) should be used instead."),
  ;

  private static final Map<String, ServiceExceptionMessages> descriptions =
      Collections.unmodifiableMap(
          Stream.of(values())
              .collect(
                  Collectors.toMap(ServiceExceptionMessages::getDescription, Function.identity())));

  @Getter private final HttpStatus code;
  @Getter private final String description;

  ServiceExceptionMessages(HttpStatus code, String description) {
    this.code = code;
    this.description = description;
  }

  // NOTE : description 으로 enum key 찾기
  public static ServiceExceptionMessages findEnumKey(String description) {
    return descriptions.get(description);
  }

  public String getStatusCode() {
    return String.valueOf(this.code.value());
  }
}
