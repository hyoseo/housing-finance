package me.hyoseo.housingfinance.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

import java.util.LinkedHashMap;
import java.util.Map;

@Getter
@Setter
public class YearlyInstitutesSupport {
    public YearlyInstitutesSupport(Short year) {
        this.year = year;
    }

    private Short year;

    @JsonProperty("total_amount")
    private Long totalAmount = 0L;

    @JsonProperty("detail_amount")
    private Map<String, Long> detailAmount = new LinkedHashMap<>();
}
