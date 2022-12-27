package org.example.config;

import javax.annotation.PostConstruct;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.ConfigurationProperties;

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
