package me.hyoseo.housingfinance.database.model;


import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@Data
@NoArgsConstructor
@Entity
@Table(name = "institutions")
public class Institution {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "institute_code")
    private Integer code;

    @Column(name = "institute_name", unique = true)
    private String name;

    public Institution(String name) {
        this.name = name;
    }
}
