package me.hyoseo.housingfinance.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.hyoseo.housingfinance.database.model.Institute;
import me.hyoseo.housingfinance.database.model.InstituteSupport;
import me.hyoseo.housingfinance.database.repository.InstituteRepository;
import me.hyoseo.housingfinance.database.repository.InstituteSupportRepository;
import me.hyoseo.housingfinance.error.CommonException;
import me.hyoseo.housingfinance.error.ErrorCode;
import me.hyoseo.housingfinance.response.AppliedFile;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.transaction.Transactional;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;

@Slf4j
@RequiredArgsConstructor
@RequestMapping("/data")
@RestController
public class DataFileController {

    private final InstituteRepository instituteRepository;

    private final InstituteSupportRepository instituteSupportRepository;

    @Transactional
    @PostMapping("/csv_files")
    public ResponseEntity<AppliedFile> applyFileToDatabase(
            @RequestPart(value = "csv_file", required = false) MultipartFile csvFile,
            @RequestParam(value = "bank_name_end_chars", defaultValue = "1,(") List<Character> bankNameEndChars) {
        Resource resource = (csvFile != null) ? csvFile.getResource() : new ClassPathResource("/static/사전과제3.csv");

        try (Scanner scanner = new Scanner(resource.getInputStream())) {
            List<String> bankNames = new ArrayList<>(Arrays.asList("", ""));

            // 첫 번째 행 (은행 이름들 처리)
            if (scanner.hasNextLine()) {
                List<String> record = getRecordFromLine(scanner.nextLine());

                record.subList(2, record.size()).stream()
                        .map(bankName -> {
                            String refinedBankName = bankName;
                            for (Character bankNameEndChar : bankNameEndChars) {
                                int found_index_pos = bankName.indexOf(bankNameEndChar);
                                if (found_index_pos != -1) {
                                    refinedBankName = bankName.substring(0, found_index_pos);
                                    break;
                                }
                            }
                            bankNames.add(refinedBankName);
                            return refinedBankName;
                        })
                        .filter(bankName -> instituteRepository.findByName(bankName).isPresent() == false)
                        .forEach(bankName -> instituteRepository.save(new Institute(bankName)));
            }

            // 그 외의 행들 (각 금융기관 지원 금액 처리)
            while (scanner.hasNextLine()) {
                List<String> record = getRecordFromLine(scanner.nextLine());
                String year = record.get(0);
                String month = record.get(1);

                for (int col = 2; col < record.size(); ++col) {
                    instituteSupportRepository.save(new InstituteSupport(
                            Short.valueOf(year),
                            Byte.valueOf(month),
                            instituteRepository.findByName(bankNames.get(col)).get(),
                            Integer.valueOf(record.get(col))));
                }
            }
        } catch (Exception ex) {
            throw CommonException.create(ErrorCode.INTERNAL_SERVER_ERROR, ex);
        }

        return ResponseEntity.ok().body(new AppliedFile(resource.getFilename(), bankNameEndChars));
    }

    private List<String> getRecordFromLine(String line) {
        List<String> values = new ArrayList<>();

        try (Scanner rowScanner = new Scanner(line)) {
            rowScanner.useDelimiter(",");
            while (rowScanner.hasNext()) {
                String row = rowScanner.next();
                if (row.startsWith("\"")){
                    StringBuilder composited_row = new StringBuilder();
                    composited_row.append(row, 1, row.length());

                    while (rowScanner.hasNext()) {
                        String related_row = rowScanner.next();
                        if (related_row.endsWith("\"")) {
                            composited_row.append(related_row, 0, related_row.length() - 1);
                            break;
                        } else {
                            composited_row.append(related_row);
                        }
                    }

                    row = composited_row.toString();
                }

                if (row.isEmpty() == false)
                    values.add(row);
            }
        }

        return values;
    }
}
