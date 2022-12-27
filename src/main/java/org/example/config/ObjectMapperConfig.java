package org.example.config;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;

@Configuration
public class ObjectMapperConfig {
  /**
   * 활성화 된 것들 : ACCEPT_CASE_INSENSITIVE_PROPERTIES : 대소문자 구분
   *
   * <p>ACCEPT_SINGLE_VALUE_AS_ARRAY : 타 사이트와 Json 으로 데이터 통신을 할경우, 명세에는 분명 LIST로 되어 있는데, 결과가 하나인 경우
   * 단일 데이터로 보내는 경우가 있다. 이런식이 명세인데... { "names" : [ "Jhon", "Doe" ] } 하나의 경우 이렇게 보내는.. { "names" :
   * "Jhon" }
   *
   * <p>비활성 된 것들 : WRITE_DATES_AS_TIMESTAMPS : timestamp로 저장하지 않기로 설정
   *
   * <p>FAIL_ON_UNKNOWN_PROPERTIES : POJO 에 정의 되지 않은 필드는 무시
   *
   * @return
   */
  public static ObjectMapper newWebclientObjectMapper() {
    return Jackson2ObjectMapperBuilder.json()
        .serializationInclusion(NON_NULL)
        .failOnEmptyBeans(false)
        .failOnUnknownProperties(false)
        .featuresToEnable(
            MapperFeature.ACCEPT_CASE_INSENSITIVE_PROPERTIES,
            DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY)
        .featuresToDisable(
            SerializationFeature.WRITE_DATES_AS_TIMESTAMPS,
            DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
        .build();
  }

  @Bean
  public ObjectMapper webClientObjectMapper() {
    return newWebclientObjectMapper();
  }

  @Primary
  @Bean
  public ObjectMapper defaultObjectMapper() {
    return new Jackson2ObjectMapperBuilder().modulesToInstall(new JavaTimeModule()).build();
  }
}
