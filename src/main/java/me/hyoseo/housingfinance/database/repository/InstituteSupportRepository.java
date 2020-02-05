package me.hyoseo.housingfinance.database.repository;

import me.hyoseo.housingfinance.database.model.InstituteMonthlyAvgSupport;
import me.hyoseo.housingfinance.database.model.InstituteSupport;
import me.hyoseo.housingfinance.database.model.InstituteSupportId;
import me.hyoseo.housingfinance.database.model.InstituteYearlySupport;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface InstituteSupportRepository extends JpaRepository<InstituteSupport, InstituteSupportId> {

    @Query("SELECT InsSupport.year AS year, InsSupport.institute AS institute, " +
            "SUM(InsSupport.supportAmount) AS supportAmount " +
            "FROM InstituteSupport InsSupport GROUP BY InsSupport.year, InsSupport.institute")
    List<InstituteYearlySupport> findYearlySupport();

    @Query("SELECT InsSupport.year AS year, InsSupport.institute AS institute, " +
            "SUM(InsSupport.supportAmount) AS supportAmount " +
            "FROM InstituteSupport InsSupport GROUP BY InsSupport.year, InsSupport.institute " +
            "ORDER BY SUM(InsSupport.supportAmount) DESC")
    Page<InstituteYearlySupport> findYearlyTopSupportInstitutes(Pageable pageable);

    @Query("SELECT InsSupport.year AS year, InsSupport.institute AS institute, " +
            "AVG(InsSupport.supportAmount) AS avgSupportAmount FROM " +
            "InstituteSupport InsSupport where InsSupport.institute.code = :institute_code " +
            "GROUP BY InsSupport.year, InsSupport.institute ORDER BY avgSupportAmount DESC")
    List<InstituteMonthlyAvgSupport> findMonthlyAvgSupport(@Param("institute_code") Integer instituteCode);
}
