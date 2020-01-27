package me.hyoseo.housingfinance.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

@Getter
@AllArgsConstructor
class YearAvgSupport {
    private Short year;

    private Long amount;
}

@Getter
public class InstituteAvgMinMaxSupport {
    public InstituteAvgMinMaxSupport(String bank) {
        this.bank = bank;
    }

    private String bank;

    @JsonProperty("support_amount")
    private List<YearAvgSupport> yearAvgSupportList = new ArrayList<>();

    public void addYearAvgSupport(Short year, Long amount) {
        yearAvgSupportList.add(new YearAvgSupport(year, amount));
    }
}
