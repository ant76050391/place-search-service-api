package org.example.handler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.dto.*;
import org.example.enums.ServiceExceptionMessages;
import org.example.exception.ServiceException;
import org.example.external.api.KakaoPlaceSearchAPI;
import org.example.external.api.NaverPlaceSearchAPI;
import org.example.repository.PlaceRepository;
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
  private final PlaceRepository placeRepository;

  public Mono<ServerResponse> search(ServerRequest serverRequest) {
    final String query = serverRequest.queryParam("query").orElse("");

    if (!StringUtils.hasText(query)) {
      return Mono.error(new ServiceException(ServiceExceptionMessages.BAD_REQUEST_QUERY_PARAMETER_REQUIRED));
    }

    Mono<PlaceSearchAPIResponse> kakaoPlaceSearchAPIResponse = kakaoPlaceSearchAPI.call(query);
    Mono<PlaceSearchAPIResponse> naverPlaceSearchAPIResponse = naverPlaceSearchAPI.call(query);
    Mono<Double> incrementSearchKeywordRankResult = placeRepository.incrementSearchKeywordScore(query);

    Mono<PlaceSearchResult> placeSearchResultMono = Mono.zip(kakaoPlaceSearchAPIResponse, naverPlaceSearchAPIResponse, incrementSearchKeywordRankResult)
        .flatMap(tuples -> {
          PlaceSearchAPIResponse kakao = tuples.getT1();
          PlaceSearchAPIResponse naver = tuples.getT2();

          kakao.getDocuments().stream().forEach(documents -> log.info("kakao {}", documents.toString()));
          naver.getDocuments().stream().forEach(documents -> log.info("naver {}", documents.toString()));

          return Mono.just(PlaceSearchResult.builder().places(Collections.singletonList(Places.builder().placeName(String.valueOf(tuples.getT3().intValue())).build())).build());
        });

    return ok()
        .contentType(APPLICATION_JSON)
        .body(placeSearchResultMono, PlaceSearchResult.class);
  }

  public Mono<ServerResponse> searchKeywords(ServerRequest serverRequest) {

    Mono<List<SearchKeywords>> searchKeywordsMono = placeRepository.getSearchKeywords()
        .map(tuple -> SearchKeywords.builder()
            .keyword(String.valueOf(tuple.getValue()))
            .count(tuple.getScore().longValue())
            .build())
        .collectList();

    Mono<PlaceSearchKeywordRankResult> placeSearchKeywordRankResult = searchKeywordsMono
        .map(searchKeywords -> Tuples.of(searchKeywords.size(), searchKeywords))
        .map(tuple2 ->
            PlaceSearchKeywordRankResult.builder()
                .total(tuple2.getT1())
                .searchKeywords(tuple2.getT2())
                .build());

    return ok()
        .contentType(APPLICATION_JSON)
        .body(placeSearchKeywordRankResult, PlaceSearchKeywordRankResult.class);
  }

}
