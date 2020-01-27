package me.hyoseo.housingfinance.database.repository;

import me.hyoseo.housingfinance.database.model.InstituteSupport;
import me.hyoseo.housingfinance.database.model.InstituteSupportId;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import javax.persistence.Tuple;
import java.util.List;

public interface InstituteSupportRepository extends JpaRepository<InstituteSupport, InstituteSupportId> {

    @Query("SELECT InsSupport.year, InsSupport.institute, SUM(InsSupport.supportAmount) " +
            "FROM InstituteSupport InsSupport GROUP BY InsSupport.year, InsSupport.institute")
    List<Tuple> findYearlySupport();

    @Query("SELECT InsSupport.year, InsSupport.institute, SUM(InsSupport.supportAmount) " +
            "FROM InstituteSupport InsSupport GROUP BY InsSupport.year, InsSupport.institute " +
            "ORDER BY SUM(InsSupport.supportAmount) DESC")
    List<Tuple> findYearlyTopSupportInstitutes(Pageable pageable);

    @Query("SELECT InsSupport.year, InsSupport.institute, AVG(InsSupport.supportAmount) AS support_avg FROM " +
            "InstituteSupport InsSupport where InsSupport.institute.code = :institute_code " +
            "GROUP BY InsSupport.year, InsSupport.institute ORDER BY support_avg DESC")
    List<Tuple> findYearlyAvgSupport(@Param("institute_code") Integer instituteCode);
}
