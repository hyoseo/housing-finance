package me.hyoseo.housingfinance.database.repository;

import me.hyoseo.housingfinance.database.model.InstitutionSupport;
import me.hyoseo.housingfinance.database.model.InstitutionSupportId;
import org.springframework.data.jpa.repository.JpaRepository;

public interface InstitutionSupportRepository extends JpaRepository<InstitutionSupport, InstitutionSupportId> {
}
