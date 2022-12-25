package org.example.config;

import org.modelmapper.ModelMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import static org.modelmapper.config.Configuration.AccessLevel.PRIVATE;
import static org.modelmapper.convention.MatchingStrategies.STRICT;

@Configuration
public class ModelMapperConfig {
  @Bean
  public ModelMapper modelMapper() {
    ModelMapper modelMapper = new ModelMapper();
    modelMapper.getConfiguration()
        .setMatchingStrategy(STRICT)
        .setFieldMatchingEnabled(true)
        .setFieldAccessLevel(PRIVATE)
        .setSkipNullEnabled(true)
        .setCollectionsMergeEnabled(false);
    return modelMapper;

  }
}
