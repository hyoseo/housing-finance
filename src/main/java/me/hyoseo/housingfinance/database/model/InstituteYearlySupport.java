package me.hyoseo.housingfinance.database.model;

public interface InstituteYearlySupport {
    Short getYear();

    Institute getInstitute();

    Integer getSupportAmount();
}
