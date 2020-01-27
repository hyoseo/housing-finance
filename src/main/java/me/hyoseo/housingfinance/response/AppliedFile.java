package me.hyoseo.housingfinance.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
public class AppliedFile {
    @JsonProperty("applied_file_name")
    String name;

    @JsonProperty("bank_name_end_chars")
    List<Character> bankNameEndChars;
}
