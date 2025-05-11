package com.mycompany.user.grpc;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hubspot.jackson.datatype.protobuf.ProtobufModule;
import java.util.Arrays;
import java.util.List;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.http.converter.protobuf.ProtobufHttpMessageConverter;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class ProtobufConfig implements WebMvcConfigurer {

  @Bean
  public ProtobufHttpMessageConverter protobufHttpMessageConverter() {
    return new ProtobufHttpMessageConverter();
  }

  @Bean
  public MappingJackson2HttpMessageConverter protobufJsonConverter() {
    ObjectMapper mapper = new ObjectMapper();
    mapper.registerModule(new ProtobufModule());

    MappingJackson2HttpMessageConverter conv = new MappingJackson2HttpMessageConverter(mapper);
    conv.setSupportedMediaTypes(
        Arrays.asList(
            MediaType.APPLICATION_JSON, MediaType.valueOf("application/json;charset=UTF-8")));
    return conv;
  }

  @Override
  public void extendMessageConverters(List<HttpMessageConverter<?>> converters) {
    converters.add(protobufHttpMessageConverter());
    converters.add(protobufJsonConverter());
  }
}
