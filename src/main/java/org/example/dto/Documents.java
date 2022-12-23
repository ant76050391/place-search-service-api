package org.example.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.boot.context.properties.ConstructorBinding;

@Data
@Builder(toBuilder = true)
@AllArgsConstructor
@NoArgsConstructor
@ConstructorBinding
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Documents {
  @JsonAlias({"place_name", "title"})
  private String placeName;
  @JsonAlias({"address_name", "address"})
  private String addressName;
  @JsonAlias({"road_address_name", "roadAddress"})
  private String roadAddressName;
  @JsonAlias({"phone", "telephone"})
  private String phone;
  @JsonAlias({"place_url", "link"})
  private String placeUrl;
}