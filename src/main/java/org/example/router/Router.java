package org.example.router;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.handler.CommonHandler;
import org.example.handler.PlaceHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.server.RouterFunction;

import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.web.reactive.function.server.RequestPredicates.accept;
import static org.springframework.web.reactive.function.server.RouterFunctions.route;

@Slf4j
@Configuration
@RequiredArgsConstructor
@SuppressWarnings("Duplicates")
public class Router {

  private final CommonHandler commonHandler;
  private final PlaceHandler placeHandler;

  /**
   * content type 선언은 post/put/patch 만 추가
   * query parameter 사용시 같은 이름의 다른 값을 넣의 구체적 표준이 정해진 건 없지만
   * and 일때는 ?id=a&id=b or 일때는 ?id=a,b 를 사용 하자
   */

  @Bean
  public RouterFunction<?> commonRouters() {
    return route()
        .GET("/health-check", accept(APPLICATION_JSON), commonHandler::healthCheck)
        .GET("/access-denied", accept(APPLICATION_JSON), commonHandler::accessDenied)
        .build();
  }

  @Bean
  public RouterFunction<?> placesRouters() {
    return route().path("/v1", builder -> builder
            .GET("/place/search", accept(APPLICATION_JSON), placeHandler::search)
            .GET("/place/search/keyword/rank", accept(APPLICATION_JSON), placeHandler::searchKeywordRank))
        .build();
  }

}
