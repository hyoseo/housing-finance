package me.hyoseo.housingfinance.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class TotalInstituteSupport {
    String name;

    @JsonProperty("yearly_institutes_supports")
    List<YearlyInstitutesSupport> yearlyInstitutesSupportList = new ArrayList<>();
}
