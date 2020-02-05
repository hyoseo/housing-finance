package me.hyoseo.housingfinance.controller;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import me.hyoseo.housingfinance.database.model.Institute;
import me.hyoseo.housingfinance.database.model.InstituteMonthlyAvgSupport;
import me.hyoseo.housingfinance.database.model.InstituteYearlySupport;
import me.hyoseo.housingfinance.database.repository.InstituteRepository;
import me.hyoseo.housingfinance.database.repository.InstituteSupportRepository;
import me.hyoseo.housingfinance.error.CommonException;
import me.hyoseo.housingfinance.error.ErrorCode;
import me.hyoseo.housingfinance.response.InstituteAvgMinMaxSupport;
import me.hyoseo.housingfinance.response.TopInstituteSupport;
import me.hyoseo.housingfinance.response.TotalInstituteSupport;
import me.hyoseo.housingfinance.response.YearlyInstitutesSupport;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Api(tags = {"기관 컨트롤러"})
@RequiredArgsConstructor
@RequestMapping("/institutes")
@RestController
public class InstituteController {

    private final InstituteRepository instituteRepository;

    private final InstituteSupportRepository instituteSupportRepository;

    @ApiOperation(value = "주택금융 공급 금융기관(은행) 목록", response = ResponseEntity.class)
    @ApiImplicitParams(@ApiImplicitParam(name = "Access-Token", value = "Access-Token 필요", paramType = "header"))
    @GetMapping
    public ResponseEntity<List<Institute>> getInstitutes() {
        return ResponseEntity.ok(instituteRepository.findAll());
    }

    @ApiOperation(value = "년도별 각 금융기관의 지원금액 합계", response = ResponseEntity.class)
    @ApiImplicitParams(@ApiImplicitParam(name = "Access-Token", value = "Access-Token 필요", paramType = "header"))
    @GetMapping("/total_institutes_support")
    public ResponseEntity<TotalInstituteSupport> getTotalInstitutesSupport() {
        TotalInstituteSupport totalInstituteSupport = new TotalInstituteSupport();
        totalInstituteSupport.setName("주택금융 공급현황");

        Short lastYear = 0;
        YearlyInstitutesSupport yearlyInstitutesSupport = null;

        for (InstituteYearlySupport instituteYearlySupport : instituteSupportRepository.findYearlySupport()) {
            if (instituteYearlySupport.getYear().equals(lastYear) == false) {
                lastYear = instituteYearlySupport.getYear();
                yearlyInstitutesSupport = new YearlyInstitutesSupport(instituteYearlySupport.getYear());
                totalInstituteSupport.getYearlyInstitutesSupportList().add(yearlyInstitutesSupport);
            }

            yearlyInstitutesSupport.getDetailAmount()
                    .put(instituteYearlySupport.getInstitute().getName(), Long.valueOf(instituteYearlySupport.getSupportAmount()));
            yearlyInstitutesSupport.setTotalAmount(yearlyInstitutesSupport.getTotalAmount() + instituteYearlySupport.getSupportAmount());
        }

        return ResponseEntity.ok(totalInstituteSupport);
    }

    @ApiOperation(value = "각 년도별 각 기관의 전체 지원금액 중에서 가장 큰 금액의 기관명", response = ResponseEntity.class)
    @ApiImplicitParams(@ApiImplicitParam(name = "Access-Token", value = "Access-Token 필요", paramType = "header"))
    @GetMapping("/top_institute_support")
    public ResponseEntity<TopInstituteSupport> getTopInstituteSupport() {
        Page<InstituteYearlySupport> yearlyTopSupportInstitutes = instituteSupportRepository
                .findYearlyTopSupportInstitutes(PageRequest.of(0, 1));

        if (yearlyTopSupportInstitutes.hasContent() == false)
            throw CommonException.create(ErrorCode.NOT_FOUND);

        return ResponseEntity.ok(new TopInstituteSupport(yearlyTopSupportInstitutes.getContent().get(0).getYear(),
                yearlyTopSupportInstitutes.getContent().get(0).getInstitute().getName()));
    }

    @ApiOperation(value = "전체 년도에서 외환은행의 지원금액 평균 중에서 가장 작은 금액과 큰 금액", response = ResponseEntity.class)
    @ApiImplicitParams(@ApiImplicitParam(name = "Access-Token", value = "Access-Token 필요", paramType = "header"))
    @GetMapping("/institute_avg_min_max_support")
    public ResponseEntity<InstituteAvgMinMaxSupport> getInstituteAvgMinMaxSupport(
            @RequestParam(value = "bank", defaultValue = "외환은행") String bank) {
        Institute institute = instituteRepository.findByName(bank)
                .orElseThrow(() -> CommonException.create(ErrorCode.NOT_FOUND));

        List<InstituteMonthlyAvgSupport> monthlyAvgSupports = instituteSupportRepository.findMonthlyAvgSupport(institute.getCode());
        if (monthlyAvgSupports.size() < 2)
            throw CommonException.create(ErrorCode.NOT_FOUND);

        InstituteAvgMinMaxSupport instituteAvgMinMaxSupport = new InstituteAvgMinMaxSupport(institute.getName());
        instituteAvgMinMaxSupport.addYearAvgSupport(
                monthlyAvgSupports.get(monthlyAvgSupports.size() - 1).getYear(),
                Math.round(monthlyAvgSupports.get(monthlyAvgSupports.size() - 1).getAvgSupportAmount()));
        instituteAvgMinMaxSupport.addYearAvgSupport(
                monthlyAvgSupports.get(0).getYear(),
                Math.round(monthlyAvgSupports.get(0).getAvgSupportAmount()));

        return ResponseEntity.ok(instituteAvgMinMaxSupport);
    }

}
