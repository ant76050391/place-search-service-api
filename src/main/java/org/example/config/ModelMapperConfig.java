package org.example.config;

import static org.modelmapper.config.Configuration.AccessLevel.PRIVATE;
import static org.modelmapper.convention.MatchingStrategies.STRICT;

import org.modelmapper.ModelMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ModelMapperConfig {
  @Bean
  public ModelMapper modelMapper() {
    ModelMapper modelMapper = new ModelMapper();
    modelMapper
        .getConfiguration()
        .setMatchingStrategy(STRICT)
        .setFieldMatchingEnabled(true)
        .setFieldAccessLevel(PRIVATE)
        .setSkipNullEnabled(true)
        .setCollectionsMergeEnabled(false);
    return modelMapper;
  }
}
