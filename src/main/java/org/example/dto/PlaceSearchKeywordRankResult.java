package org.example.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import java.util.List;
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
public class PlaceSearchKeywordRankResult {
  private int total;
  private List<SearchKeywords> searchKeywords;
}
