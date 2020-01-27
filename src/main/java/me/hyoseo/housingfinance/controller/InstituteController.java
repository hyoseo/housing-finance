package me.hyoseo.housingfinance.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.hyoseo.housingfinance.database.model.Institute;
import me.hyoseo.housingfinance.database.repository.InstituteRepository;
import me.hyoseo.housingfinance.database.repository.InstituteSupportRepository;
import me.hyoseo.housingfinance.error.CommonException;
import me.hyoseo.housingfinance.error.ErrorCode;
import me.hyoseo.housingfinance.response.InstituteAvgMinMaxSupport;
import me.hyoseo.housingfinance.response.TopInstituteSupport;
import me.hyoseo.housingfinance.response.TotalInstituteSupport;
import me.hyoseo.housingfinance.response.YearlyInstitutesSupport;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.persistence.Tuple;
import java.util.List;

@Slf4j
@RequiredArgsConstructor
@RequestMapping("/institutes")
@RestController
public class InstituteController {

    private final InstituteRepository instituteRepository;

    private final InstituteSupportRepository instituteSupportRepository;

    // 주택금융 공급 금융기관(은행) 목록
    @GetMapping
    public ResponseEntity<List<Institute>> getInstitutes() {
        return ResponseEntity.ok(instituteRepository.findAll());
    }

    // 년도별 각 금융기관의 지원금액 합계
    @GetMapping("/total_institutes_support")
    public ResponseEntity<TotalInstituteSupport> getTotalInstitutesSupport() {
        TotalInstituteSupport totalInstituteSupport = new TotalInstituteSupport();
        totalInstituteSupport.setName("주택금융 공급현황");

        Short lastYear = 0;
        YearlyInstitutesSupport yearlyInstitutesSupport = null;
        for (Tuple tuple : instituteSupportRepository.findYearlySupport()) {
            Short year = (Short) tuple.get(0);
            Institute institute = (Institute) tuple.get(1);
            Long supportAmount = (Long) tuple.get(2);

            if (year.equals(lastYear) == false) {
                lastYear = year;
                yearlyInstitutesSupport = new YearlyInstitutesSupport(year);
                totalInstituteSupport.getYearlyInstitutesSupportList().add(yearlyInstitutesSupport);
            }

            yearlyInstitutesSupport.getDetailAmount().put(institute.getName(), supportAmount);
            yearlyInstitutesSupport.setTotalAmount(yearlyInstitutesSupport.getTotalAmount() + supportAmount);
        }

        return ResponseEntity.ok(totalInstituteSupport);
    }

    // 각 년도별 각 기관의 전체 지원금액 중에서 가장 큰 금액의 기관명
    @GetMapping("/top_institute_support")
    public ResponseEntity<TopInstituteSupport> getTopInstituteSupport() {
        List<Tuple> tuples = instituteSupportRepository.findYearlyTopSupportInstitutes(
                PageRequest.of(0, 1));
        if (tuples.isEmpty())
            throw CommonException.create(ErrorCode.NOT_FOUND);

        return ResponseEntity.ok(new TopInstituteSupport((Short)tuples.get(0).get(0),
                ((Institute)tuples.get(0).get(1)).getName()));
    }

    // 전체 년도에서 외환은행의 지원금액 평균 중에서 가장 작은 금액과 큰 금액
    @GetMapping("/institute_avg_min_max_support")
    public ResponseEntity<InstituteAvgMinMaxSupport> getInstituteAvgMinMaxSupport(
            @RequestParam(value = "bank", defaultValue = "외환은행") String bank) {
        Institute institute = instituteRepository.findByName(bank)
                .orElseThrow(() -> CommonException.create(ErrorCode.NOT_FOUND));
        List<Tuple> tuples = instituteSupportRepository.findYearlyAvgSupport(institute.getCode());
        if (tuples.size() < 2)
            throw CommonException.create(ErrorCode.NOT_FOUND);

        InstituteAvgMinMaxSupport instituteAvgMinMaxSupport = new InstituteAvgMinMaxSupport(institute.getName());
        instituteAvgMinMaxSupport.addYearAvgSupport((Short) tuples.get(tuples.size()-1).get(0),
                Math.round((Double)tuples.get(tuples.size()-1).get(2)));
        instituteAvgMinMaxSupport.addYearAvgSupport((Short) tuples.get(0).get(0),
                Math.round((Double)tuples.get(0).get(2)));

        return ResponseEntity.ok(instituteAvgMinMaxSupport);
    }

}
