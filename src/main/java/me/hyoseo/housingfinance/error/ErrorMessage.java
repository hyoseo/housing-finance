package me.hyoseo.housingfinance.error;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public class ErrorMessage {
    @JsonProperty("error_code")
    Integer errorCode;

    @JsonProperty("error_msg")
    String errorMsg;
}
