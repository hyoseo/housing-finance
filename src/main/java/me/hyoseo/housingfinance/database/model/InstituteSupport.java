package me.hyoseo.housingfinance.database.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "institute_supports")
@IdClass(InstituteSupportId.class)
public class InstituteSupport {
    @Id
    private Short year;

    @Id
    private Byte month;

    @Id
    @ManyToOne(targetEntity = Institute.class)
    @JoinColumn(name = "institute_code")
    private Institute institute;

    @Column(name = "support_amount")
    private Integer supportAmount;
}
