package org.example.config;

import io.lettuce.core.ClientOptions;
import io.lettuce.core.ReadFrom;
import io.lettuce.core.SocketOptions;
import java.time.Duration;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.connection.ReactiveRedisConnectionFactory;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceClientConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.data.redis.serializer.GenericToStringSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

@Slf4j
@RequiredArgsConstructor
@Configuration
@EnableConfigurationProperties(RedisProperties.class)
@SuppressWarnings("Duplicates")
public class RedisConfig {
  private final RedisProperties redisProperties;

  @Bean(name = "lettuceClientConfiguration")
  public LettuceClientConfiguration lettuceClientConfiguration() {
    return LettuceClientConfiguration.builder()
        .readFrom(ReadFrom.MASTER)
        .commandTimeout(Duration.ofSeconds(1))
        .shutdownTimeout(Duration.ZERO)
        .clientOptions(
            ClientOptions.builder()
                .autoReconnect(true)
                .publishOnScheduler(true)
                .socketOptions(SocketOptions.builder().keepAlive(true).build())
                .build())
        .build();
  }

  @Primary
  @Bean(name = "lettuceConnectionFactory")
  public ReactiveRedisConnectionFactory lettuceConnectionFactory(
      @Qualifier("lettuceClientConfiguration")
          LettuceClientConfiguration lettuceClientConfiguration) {
    RedisStandaloneConfiguration redisStandaloneConfiguration =
        new RedisStandaloneConfiguration(redisProperties.getEndpoint(), redisProperties.getPort());
    return new LettuceConnectionFactory(redisStandaloneConfiguration, lettuceClientConfiguration);
  }

  @Bean(name = "redisTemplate")
  public ReactiveRedisTemplate<String, Object> reactiveRedisOperations(
      @Qualifier("lettuceConnectionFactory")
          ReactiveRedisConnectionFactory reactiveRedisConnectionFactory) {
    log.info("{} connectionFactory {} ", "redis", reactiveRedisConnectionFactory);

    RedisSerializationContext<String, Object> serializationContext =
        RedisSerializationContext.<String, Object>newSerializationContext(
                new StringRedisSerializer())
            .key(new StringRedisSerializer())
            .value(new GenericToStringSerializer<>(Object.class))
            .hashKey(new StringRedisSerializer())
            .hashValue(new GenericToStringSerializer<>(Object.class))
            .build();

    return new ReactiveRedisTemplate<>(reactiveRedisConnectionFactory, serializationContext);
  }
}
