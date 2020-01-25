package me.hyoseo.housingfinance.database.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "institution_supports")
@IdClass(InstitutionSupportId.class)
public class InstitutionSupport {
    @Id
    private Short year;

    @Id
    private Byte month;

    @Id
    @ManyToOne(targetEntity = Institution.class)
    @JoinColumn(name = "institute_code")
    private Institution institution;

    private Integer supportAmount;
}
