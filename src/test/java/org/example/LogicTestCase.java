package org.example;

import info.debatty.java.stringsimilarity.*;
import lombok.extern.slf4j.Slf4j;
import org.example.config.ModelMapperConfig;
import org.example.dto.Documents;
import org.example.dto.Places;
import org.example.util.ListUtil;
import org.example.util.NumberUtil;
import org.example.util.StringUtil;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@ExtendWith(SpringExtension.class)
@WebFluxTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@ContextConfiguration(classes = {ModelMapperConfig.class})
public class LogicTestCase {
  @Autowired
  private ModelMapper modelMapper;

  @Test
  public void ping() {
    Mono<Boolean> booleanMono = Mono.just(true);
    StepVerifier.create(booleanMono).expectSubscription().expectNext(true).verifyComplete();
  }

  @Test
  public void placeSearch1Test() {
    List<Documents> kakao = Arrays.asList(
        Documents.builder().placeName("카카오뱅크").addressName("서울").build(),
        Documents.builder().placeName("우리은행").addressName("서울").build(),
        Documents.builder().placeName("국민은행").addressName("서울").build(),
        Documents.builder().placeName("부산은행").addressName("서울").build(),
        Documents.builder().placeName("새마을금고").addressName("서울").build());
    List<Documents> naver = Arrays.asList(
        Documents.builder().placeName("카카오뱅크").addressName("서울").build(),
        Documents.builder().placeName("부산은행").addressName("서울").build(),
        Documents.builder().placeName("하나은행").addressName("서울").build(),
        Documents.builder().placeName("국민은행").addressName("서울").build(),
        Documents.builder().placeName("기업은행").addressName("서울").build());

    List<Documents> list = new ArrayList<>();
    list.addAll(ListUtil.intersectionBy(kakao, naver));
    list.addAll(ListUtil.differenceBy(kakao, naver));
    list.addAll(ListUtil.differenceBy(naver, kakao));

    List<String> placeNames = list.stream().map(Documents::getPlaceName).collect(Collectors.toList());

    Mono<String> actualMono = Mono.just("[카카오뱅크, 국민은행, 부산은행, 우리은행, 새마을금고, 하나은행, 기업은행]");
    StepVerifier.create(actualMono).expectSubscription().expectNext(placeNames.toString()).verifyComplete();
  }

  @Test
  public void placeSearch2Test() {
    List<Documents> kakao = Arrays.asList(
        Documents.builder().placeName("A곱창").addressName("서울").build(),
        Documents.builder().placeName("B곱창").addressName("서울").build(),
        Documents.builder().placeName("C곱창").addressName("서울").build(),
        Documents.builder().placeName("D곱창").addressName("서울").build());
    List<Documents> naver = Arrays.asList(
        Documents.builder().placeName("A곱창").addressName("서울").build(),
        Documents.builder().placeName("E곱창").addressName("서울").build(),
        Documents.builder().placeName("D곱창").addressName("서울").build(),
        Documents.builder().placeName("C곱창").addressName("서울").build());

    List<Documents> list = new ArrayList<>();
    list.addAll(ListUtil.intersectionBy(kakao, naver));
    list.addAll(ListUtil.differenceBy(kakao, naver));
    list.addAll(ListUtil.differenceBy(naver, kakao));

    List<String> placeNames = list.stream().map(Documents::getPlaceName).collect(Collectors.toList());

    Mono<String> actualMono = Mono.just("[A곱창, C곱창, D곱창, B곱창, E곱창]");
    StepVerifier.create(actualMono).expectSubscription().expectNext(placeNames.toString()).verifyComplete();
  }

  @Test
  public void placeSearch3Test() {
    List<Documents> kakao = Arrays.asList(
        Documents.builder().placeName("A곱창").addressName("서울").build(),
        Documents.builder().placeName("B곱창").addressName("서울").build(),
        Documents.builder().placeName("C곱창").addressName("서울").build(),
        Documents.builder().placeName("D곱창").addressName("서울").build());
    List<Documents> naver = Arrays.asList(
        Documents.builder().placeName("A곱창").addressName("서울").build(),
        Documents.builder().placeName("E곱창").addressName("서울").build(),
        Documents.builder().placeName("D곱창").addressName("서울").build(),
        Documents.builder().placeName("C곱창").addressName("서울").build());

    List<Documents> list = new ArrayList<>();
    list.addAll(ListUtil.intersectionBy(kakao, naver));
    list.addAll(ListUtil.differenceBy(kakao, naver));
    list.addAll(ListUtil.differenceBy(naver, kakao));

    List<Places> places = ListUtil.mappingLists(modelMapper, list, Places.class);

    List<String> placeNames = places.stream().map(Places::getPlaceName).collect(Collectors.toList());

    Mono<String> actualMono = Mono.just("[A곱창, C곱창, D곱창, B곱창, E곱창]");
    StepVerifier.create(actualMono).expectSubscription().expectNext(placeNames.toString()).verifyComplete();
  }

  @Test
  public void removeWhiteSpace() {
    String text = " 가 나 다 ";
    String text2 = " 라 마 바 ";
    Mono<String> actualMono = Mono.just("가나다라마바");
    StepVerifier.create(actualMono).expectSubscription().expectNext(StringUtil.removeWhiteSpace(text, text2)).verifyComplete();
  }

  @Test
  public void removeHtmlTag() {
    String text = "가나다 <b>라마바</b>";
    Mono<String> actualMono = Mono.just("가나다 라마바");
    StepVerifier.create(actualMono).expectSubscription().expectNext(StringUtil.removeHtmlTag(text)).verifyComplete();
  }


  /**
   * 장소 와 지번 주소를 사용 하지만, 비슷한 문자열 이고 길이가 짧을 수록 정확도가 떨어진다.
   * 같은 조건에서 정확도가 제일 높은걸 찾아 본다.
   */

  @Test
  public void normalizedLevenshteinTest() {
    NormalizedLevenshtein algorithms = new NormalizedLevenshtein();
    double one = NumberUtil.decimalFormat(algorithms.similarity("카카오뱅크본점서울강북", "카카오뱅크본점서울강북"), 2);
    double two = NumberUtil.decimalFormat(algorithms.similarity("카카오뱅크본점서울강북", "카카오뱅크분점서울강북"), 2);
    double three = NumberUtil.decimalFormat(algorithms.similarity("카카오뱅크서울가북", "카카오뱅크경기성남"), 2);
    double four = NumberUtil.decimalFormat(algorithms.similarity("카카오뱅크본점서울강북", "카카오스타일경기성남"), 2);
    double five = NumberUtil.decimalFormat(algorithms.similarity("해성막창집본점부산해운대구중동1732", "해성막창집본점부산광역시해운대구중동1732"), 2);
    log.info("{}", one);
    log.info("{}", two);
    log.info("{}", three);
    log.info("{}", four);
    log.info("{}", five);
    /**
     * 1.0
     * 0.91
     * 0.56
     * 0.27
     * 0.86
     */
    Mono<Boolean> actualMono = Mono.just(Double.compare(one, 0.80) == 1);
    StepVerifier.create(actualMono).expectSubscription().expectNext(true).verifyComplete();
    actualMono = Mono.just(Double.compare(two, 0.80) == 1);
    StepVerifier.create(actualMono).expectSubscription().expectNext(true).verifyComplete();
    actualMono = Mono.just(Double.compare(three, 0.80) == 1);
    StepVerifier.create(actualMono).expectSubscription().expectNext(false).verifyComplete();
    actualMono = Mono.just(Double.compare(four, 0.80) == 1);
    StepVerifier.create(actualMono).expectSubscription().expectNext(false).verifyComplete();
    actualMono = Mono.just(Double.compare(five, 0.80) == 1);
    StepVerifier.create(actualMono).expectSubscription().expectNext(true).verifyComplete();
  }

  /**
   * jaroWinklerTest , jaroWinklerTest2 의 two 비교시
   * 실제로 장소명이나 주소가 자세해질 수록 정확도 차이가 분명 해진다. 0.95가 현재로선 적정값 일듯 하다.
   */
  @Test
  public void jaroWinklerTest() {
    /**
     * 1.0
     * 0.97
     * 0.85
     * 0.52
     * 0.97
     */
    JaroWinkler algorithms = new JaroWinkler();
    double one = NumberUtil.decimalFormat(algorithms.similarity("카카오뱅크본점서울강북", "카카오뱅크본점서울강북"), 2);
    double two = NumberUtil.decimalFormat(algorithms.similarity("카카오뱅크본점서울강북", "카카오뱅크분점서울강북"), 2);
    double three = NumberUtil.decimalFormat(algorithms.similarity("카카오뱅크서울강북", "카카오뱅크경기성남"), 2);
    double four = NumberUtil.decimalFormat(algorithms.similarity("카카오뱅크본점서울강북", "카카오스타일경기성남"), 2);
    double five = NumberUtil.decimalFormat(algorithms.similarity("해성막창집본점부산해운대구중동1732", "해성막창집본점부산광역시해운대구중동1732"), 2);
    log.info("{}", one);
    log.info("{}", two);
    log.info("{}", three);
    log.info("{}", four);
    log.info("{}", five);
    Mono<Boolean> actualMono = Mono.just(Double.compare(one, 0.95) == 1);
    StepVerifier.create(actualMono).expectSubscription().expectNext(true).verifyComplete();
    actualMono = Mono.just(Double.compare(two, 0.95) == 1);
    StepVerifier.create(actualMono).expectSubscription().expectNext(true).verifyComplete();
    actualMono = Mono.just(Double.compare(three, 0.95) == 1);
    StepVerifier.create(actualMono).expectSubscription().expectNext(false).verifyComplete();
    actualMono = Mono.just(Double.compare(four, 0.95) == 1);
    StepVerifier.create(actualMono).expectSubscription().expectNext(false).verifyComplete();
    actualMono = Mono.just(Double.compare(five, 0.95) == 1);
    StepVerifier.create(actualMono).expectSubscription().expectNext(true).verifyComplete();
  }

  @Test
  public void jaroWinkler2Test() {
    /**
     * 1.0
     * 0.91
     * 0.85
     * 0.52
     * 0.97
     */
    JaroWinkler algorithms = new JaroWinkler();
    double one = NumberUtil.decimalFormat(algorithms.similarity("카카오뱅크본점서울강북", "카카오뱅크본점서울강북"), 2);
    double two = NumberUtil.decimalFormat(algorithms.similarity("카카오뱅크본점서울강북1745", "카카오뱅크분점서울강북1778"), 2);
    double three = NumberUtil.decimalFormat(algorithms.similarity("카카오뱅크서울강북", "카카오뱅크경기성남"), 2);
    double four = NumberUtil.decimalFormat(algorithms.similarity("카카오뱅크본점서울강북", "카카오스타일경기성남"), 2);
    double five = NumberUtil.decimalFormat(algorithms.similarity("해성막창집본점부산해운대구중동1732", "해성막창집본점부산광역시해운대구중동1732"), 2);
    log.info("{}", one);
    log.info("{}", two);
    log.info("{}", three);
    log.info("{}", four);
    log.info("{}", five);
    Mono<Boolean> actualMono = Mono.just(Double.compare(one, 0.95) == 1);
    StepVerifier.create(actualMono).expectSubscription().expectNext(true).verifyComplete();
    actualMono = Mono.just(Double.compare(two, 0.95) == 1);
    StepVerifier.create(actualMono).expectSubscription().expectNext(false).verifyComplete();
    actualMono = Mono.just(Double.compare(three, 0.95) == 1);
    StepVerifier.create(actualMono).expectSubscription().expectNext(false).verifyComplete();
    actualMono = Mono.just(Double.compare(four, 0.95) == 1);
    StepVerifier.create(actualMono).expectSubscription().expectNext(false).verifyComplete();
    actualMono = Mono.just(Double.compare(five, 0.95) == 1);
    StepVerifier.create(actualMono).expectSubscription().expectNext(true).verifyComplete();
  }

  @Test
  public void ratcliffObershelpTest() {
    /**
     * 1.0
     * 0.91
     * 0.56
     * 0.29
     * 0.93
     */
    RatcliffObershelp algorithms = new RatcliffObershelp();
    double one = NumberUtil.decimalFormat(algorithms.similarity("카카오뱅크본점서울강북", "카카오뱅크본점서울강북"), 2);
    double two = NumberUtil.decimalFormat(algorithms.similarity("카카오뱅크본점서울강북", "카카오뱅크분점서울강북"), 2);
    double three = NumberUtil.decimalFormat(algorithms.similarity("카카오뱅크서울강북", "카카오뱅크경기성남"), 2);
    double four = NumberUtil.decimalFormat(algorithms.similarity("카카오뱅크본점서울강북", "카카오스타일경기성남"), 2);
    double five = NumberUtil.decimalFormat(algorithms.similarity("해성막창집본점부산해운대구중동1732", "해성막창집본점부산광역시해운대구중동1732"), 2);
    log.info("{}", one);
    log.info("{}", two);
    log.info("{}", three);
    log.info("{}", four);
    log.info("{}", five);
    Mono<Boolean> actualMono = Mono.just(Double.compare(one, 0.95) == 1);
    StepVerifier.create(actualMono).expectSubscription().expectNext(true).verifyComplete();
    actualMono = Mono.just(Double.compare(two, 0.95) == 1);
    StepVerifier.create(actualMono).expectSubscription().expectNext(false).verifyComplete();
    actualMono = Mono.just(Double.compare(three, 0.95) == 1);
    StepVerifier.create(actualMono).expectSubscription().expectNext(false).verifyComplete();
    actualMono = Mono.just(Double.compare(four, 0.95) == 1);
    StepVerifier.create(actualMono).expectSubscription().expectNext(false).verifyComplete();
    actualMono = Mono.just(Double.compare(five, 0.95) == 1);
    StepVerifier.create(actualMono).expectSubscription().expectNext(false).verifyComplete();
  }

  @Test
  public void ngramTest() {
    /**
     * 0.0
     * 0.09
     * 0.39
     * 0.68
     * 0.16
     */
    NGram algorithms = new NGram(2);
    double one = NumberUtil.decimalFormat(algorithms.distance("카카오뱅크본점서울강북", "카카오뱅크본점서울강북"), 2);
    double two = NumberUtil.decimalFormat(algorithms.distance("카카오뱅크본점서울강북", "카카오뱅크분점서울강북"), 2);
    double three = NumberUtil.decimalFormat(algorithms.distance("카카오뱅크서울강북", "카카오뱅크경기성남"), 2);
    double four = NumberUtil.decimalFormat(algorithms.distance("카카오뱅크본점서울강북", "카카오스타일경기성남"), 2);
    double five = NumberUtil.decimalFormat(algorithms.distance("해성막창집본점부산해운대구중동1732", "해성막창집본점부산광역시해운대구중동1732"), 2);
    log.info("{}", one);
    log.info("{}", two);
    log.info("{}", three);
    log.info("{}", four);
    log.info("{}", five);
    Mono<Boolean> actualMono = Mono.just(Double.compare(one, 0.20) != 1);
    StepVerifier.create(actualMono).expectSubscription().expectNext(true).verifyComplete();
    actualMono = Mono.just(Double.compare(two, 0.20) != 1);
    StepVerifier.create(actualMono).expectSubscription().expectNext(true).verifyComplete();
    actualMono = Mono.just(Double.compare(three, 0.20) != 1);
    StepVerifier.create(actualMono).expectSubscription().expectNext(false).verifyComplete();
    actualMono = Mono.just(Double.compare(four, 0.20) != 1);
    StepVerifier.create(actualMono).expectSubscription().expectNext(false).verifyComplete();
    actualMono = Mono.just(Double.compare(five, 0.20) != 1);
    StepVerifier.create(actualMono).expectSubscription().expectNext(true).verifyComplete();
  }

  @Test
  public void qgramTest() {
    /**
     * 0.0
     * 4.0
     * 8.0
     * 15.0
     * 5.0
     */
    QGram algorithms = new QGram(2);
    double one = NumberUtil.decimalFormat(algorithms.distance("카카오뱅크본점서울강북", "카카오뱅크본점서울강북"), 2);
    double two = NumberUtil.decimalFormat(algorithms.distance("카카오뱅크본점서울강북", "카카오뱅크분점서울강북"), 2);
    double three = NumberUtil.decimalFormat(algorithms.distance("카카오뱅크서울강북", "카카오뱅크경기성남"), 2);
    double four = NumberUtil.decimalFormat(algorithms.distance("카카오뱅크본점서울강북", "카카오스타일경기성남"), 2);
    double five = NumberUtil.decimalFormat(algorithms.distance("해성막창집본점부산해운대구중동1732", "해성막창집본점부산광역시해운대구중동1732"), 2);
    log.info("{}", one);
    log.info("{}", two);
    log.info("{}", three);
    log.info("{}", four);
    log.info("{}", five);
    Mono<Boolean> actualMono = Mono.just(Double.compare(one, 0.95) == 1);
    StepVerifier.create(actualMono).expectSubscription().expectNext(true).verifyComplete();
    actualMono = Mono.just(Double.compare(two, 0.95) == 1);
    StepVerifier.create(actualMono).expectSubscription().expectNext(true).verifyComplete();
    actualMono = Mono.just(Double.compare(three, 0.95) == 1);
    StepVerifier.create(actualMono).expectSubscription().expectNext(false).verifyComplete();
    actualMono = Mono.just(Double.compare(four, 0.95) == 1);
    StepVerifier.create(actualMono).expectSubscription().expectNext(false).verifyComplete();
    actualMono = Mono.just(Double.compare(five, 0.95) == 1);
    StepVerifier.create(actualMono).expectSubscription().expectNext(true).verifyComplete();
  }
}
