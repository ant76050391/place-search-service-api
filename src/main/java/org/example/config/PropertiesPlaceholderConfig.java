package org.example.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.core.io.ClassPathResource;

@Configuration
public class PropertiesPlaceholderConfig {
  @Bean
  public static PropertySourcesPlaceholderConfigurer gitPropertiesPlaceholderConfigurer() {
    PropertySourcesPlaceholderConfigurer propsConfig = new PropertySourcesPlaceholderConfigurer();
    propsConfig.setLocation(new ClassPathResource("git.properties"));
    propsConfig.setIgnoreResourceNotFound(true);
    propsConfig.setIgnoreUnresolvablePlaceholders(true);
    propsConfig.setFileEncoding("UTF-8"); // NOTE: 파일이 만들어지는 환경에 따라 인코딩을 다르게 해야할 수 있다.
    return propsConfig;
  }

}
