package org.example.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import info.debatty.java.stringsimilarity.JaroWinkler;
import lombok.*;
import org.example.util.NumberUtil;
import org.example.util.StringUtil;
import org.springframework.boot.context.properties.ConstructorBinding;

@Data
@Builder(toBuilder = true)
@AllArgsConstructor
@NoArgsConstructor
@ConstructorBinding
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Documents {
  @JsonAlias({"category_name", "category"})
  private String categoryName;

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

  private String source;

  @Override
  public boolean equals(Object o) {
    if (o instanceof Documents) {
      Documents documents = (Documents) o;
      // NOTE : 장소명+지번주소가 같거나 문자열 유사도가 높으면 같은 장소로 판정
      String placeNameAndAddressA =
          StringUtil.removeHtmlTag(StringUtil.removeWhiteSpace(this.placeName, this.addressName));
      String placeNameAndAddressB =
          StringUtil.removeHtmlTag(
              StringUtil.removeWhiteSpace(documents.getPlaceName(), documents.getAddressName()));
      JaroWinkler jw = new JaroWinkler();
      double similarity =
          NumberUtil.decimalFormat(jw.similarity(placeNameAndAddressA, placeNameAndAddressB), 2);
      return placeNameAndAddressA.equals(placeNameAndAddressB)
          || (Double.compare(similarity, 0.95) == 1); // 값 비교
    }
    return false;
  }

  @Override
  public int hashCode() {
    return StringUtil.removeHtmlTag(StringUtil.removeWhiteSpace(this.placeName, this.addressName))
        .hashCode();
  }
}
