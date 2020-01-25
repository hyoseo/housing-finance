package me.hyoseo.housingfinance.database.repository;

import me.hyoseo.housingfinance.database.model.Institution;
import org.springframework.data.jpa.repository.JpaRepository;

public interface InstitutionRepository extends JpaRepository<Institution, Integer> {
}
