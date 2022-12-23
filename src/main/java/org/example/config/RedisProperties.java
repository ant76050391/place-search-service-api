package org.example.config;

import lombok.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.ConfigurationProperties;

import javax.annotation.PostConstruct;
import java.util.List;
import java.util.Set;

@Slf4j
@ConfigurationProperties(prefix = "place.redis")
@Data
@ToString
@NoArgsConstructor
public class RedisProperties {
  private String endpoint;
  private int port;

  @PostConstruct
  public void init() {
    log.info("+ {}", this);
  }

}
