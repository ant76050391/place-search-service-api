package org.example.handler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.dto.PlaceSearchAPIResponse;
import org.example.dto.PlaceSearchKeywordRankResult;
import org.example.dto.PlaceSearchResult;
import org.example.dto.Places;
import org.example.enums.ServiceExceptionMessages;
import org.example.exception.ServiceException;
import org.example.external.api.KakaoPlaceSearchAPI;
import org.example.external.api.NaverPlaceSearchAPI;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;
import reactor.util.function.Tuples;

import java.util.Collections;
import java.util.List;

import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.web.reactive.function.server.ServerResponse.ok;

@Slf4j
@Component
@RequiredArgsConstructor
public class PlaceHandler {
  private final KakaoPlaceSearchAPI kakaoPlaceSearchAPI;
  private final NaverPlaceSearchAPI naverPlaceSearchAPI;

  public Mono<ServerResponse> search(ServerRequest serverRequest) {
    final String query = serverRequest.queryParam("query").orElse("");

    if (!StringUtils.hasText(query)) {
      return Mono.error(new ServiceException(ServiceExceptionMessages.BAD_REQUEST_QUERY_PARAMETER_REQUIRED));
    }

    Mono<PlaceSearchAPIResponse> kakaoPlaceSearchAPIResponse = kakaoPlaceSearchAPI.call(query);
    Mono<PlaceSearchAPIResponse> naverPlaceSearchAPIResponse = naverPlaceSearchAPI.call(query);

    Mono<PlaceSearchResult> placeSearchResult = Mono.zip(kakaoPlaceSearchAPIResponse, naverPlaceSearchAPIResponse)
        .map(tuples -> {
          PlaceSearchAPIResponse kakao = tuples.getT1();
          PlaceSearchAPIResponse naver = tuples.getT2();

          return PlaceSearchResult.builder().places(Collections.singletonList(Places.builder().placeName("타잔").build())).build();
        });

    return ok()
        .contentType(APPLICATION_JSON)
        .body(placeSearchResult, PlaceSearchResult.class);
  }

  public Mono<ServerResponse> searchKeywordRank(ServerRequest serverRequest) {
    PlaceSearchResult placeSearchResult = PlaceSearchResult.builder().places(Collections.singletonList(Places.builder().placeName("타잔").build())).build();
    return ok()
        .contentType(APPLICATION_JSON)
        .body(placeSearchResult, PlaceSearchKeywordRankResult.class);
  }

}
