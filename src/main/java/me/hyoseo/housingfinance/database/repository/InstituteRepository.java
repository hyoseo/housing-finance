package me.hyoseo.housingfinance.database.repository;

import me.hyoseo.housingfinance.database.model.Institute;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface InstituteRepository extends JpaRepository<Institute, Integer> {

    Optional<Institute> findByName(String name);
}
