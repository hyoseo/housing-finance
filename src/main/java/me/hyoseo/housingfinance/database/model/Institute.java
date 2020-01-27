package me.hyoseo.housingfinance.database.model;


import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@Data
@NoArgsConstructor
@Entity
@Table(name = "institutes")
public class Institute {
    public Institute(String name) {
        this.name = name;
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "institute_code")
    private Integer code;

    @Column(name = "institute_name", unique = true)
    private String name;
}
