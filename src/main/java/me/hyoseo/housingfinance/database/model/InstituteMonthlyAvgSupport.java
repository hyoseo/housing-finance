package me.hyoseo.housingfinance.database.model;

public interface InstituteMonthlyAvgSupport {
    Short getYear();

    Institute getInstitute();

    Double getAvgSupportAmount();
}
