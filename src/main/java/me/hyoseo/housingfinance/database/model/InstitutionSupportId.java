package me.hyoseo.housingfinance.database.model;

import lombok.Data;

import java.io.Serializable;

@Data
public class InstitutionSupportId implements Serializable {
    private Short year;

    private Byte month;

    private Integer institution;
}
