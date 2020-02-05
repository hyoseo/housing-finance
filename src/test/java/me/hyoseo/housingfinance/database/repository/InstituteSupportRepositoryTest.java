package me.hyoseo.housingfinance.database.repository;

import me.hyoseo.housingfinance.database.model.Institute;
import me.hyoseo.housingfinance.database.model.InstituteMonthlyAvgSupport;
import me.hyoseo.housingfinance.database.model.InstituteSupport;
import me.hyoseo.housingfinance.database.model.InstituteYearlySupport;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.List;
import java.util.NoSuchElementException;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(SpringRunner.class)
@DataJpaTest
public class InstituteSupportRepositoryTest {

    @Autowired
    InstituteRepository instituteRepository;

    @Autowired
    InstituteSupportRepository instituteSupportRepository;

    @Before
    public void setUp() {
        Institute institute = new Institute("주택도시기금");
        instituteRepository.save(institute);
        instituteSupportRepository.save(new InstituteSupport((short)2005, (byte)1, institute, 1019));
        instituteSupportRepository.save(new InstituteSupport((short)2005, (byte)2, institute, 1144));
        instituteSupportRepository.save(new InstituteSupport((short)2006, (byte)1, institute, 1214));
        instituteSupportRepository.save(new InstituteSupport((short)2006, (byte)2, institute, 1618));

        institute = new Institute("국민은행");
        instituteRepository.save(institute);
        instituteSupportRepository.save(new InstituteSupport((short)2005, (byte)1, institute, 846));
        instituteSupportRepository.save(new InstituteSupport((short)2005, (byte)2, institute, 864));
        instituteSupportRepository.save(new InstituteSupport((short)2006, (byte)1, institute, 534));
        instituteSupportRepository.save(new InstituteSupport((short)2006, (byte)2, institute, 416));
    }

    @Test
    public void findYearlySupport() {
        List<InstituteYearlySupport> instituteYearlySupportList = instituteSupportRepository.findYearlySupport();
        assertThat(instituteYearlySupportList)
                .isNotEmpty()
                .hasSize(4)
                .flatExtracting(
                        InstituteYearlySupport::getYear,
                        (instituteYearlySupport) -> instituteYearlySupport.getInstitute().getName(),
                        InstituteYearlySupport::getSupportAmount)
                .containsExactly(
                        (short)2005, "주택도시기금", 2163, (short)2005, "국민은행", 1710,
                        (short)2006, "주택도시기금", 2832, (short)2006, "국민은행", 950);
    }

    @Test
    public void findYearlyTopSupportInstitutes() {
        Page<InstituteYearlySupport> yearlyTopSupportInstitutes = instituteSupportRepository
                .findYearlyTopSupportInstitutes(PageRequest.of(0, 1));

        assertThat(yearlyTopSupportInstitutes.getContent())
                .isNotEmpty()
                .hasSize(1)
                .flatExtracting(
                        InstituteYearlySupport::getYear,
                        (instituteYearlySupport) -> instituteYearlySupport.getInstitute().getName(),
                        InstituteYearlySupport::getSupportAmount)
                .containsExactly((short)2006, "주택도시기금", 2832);

    }

    @Test
    public void findMonthlyAvgSupport() {
        List<InstituteMonthlyAvgSupport> monthlyAvgSupports = instituteSupportRepository
                .findMonthlyAvgSupport(instituteRepository.findByName("국민은행")
                        .orElseThrow(NoSuchElementException::new).getCode());

        assertThat(monthlyAvgSupports)
                .isNotEmpty()
                .hasSize(2)
                .flatExtracting(
                        InstituteMonthlyAvgSupport::getYear,
                        (instituteMonthlyAvgSupport) -> Math.round(instituteMonthlyAvgSupport.getAvgSupportAmount()))
                .containsExactly(
                        (short)2005, 855L, (short)2006, 475L);
    }
}