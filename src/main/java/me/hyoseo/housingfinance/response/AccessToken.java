package me.hyoseo.housingfinance.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class AccessToken {
    @JsonProperty("access_token")
    String accessToken;
}
