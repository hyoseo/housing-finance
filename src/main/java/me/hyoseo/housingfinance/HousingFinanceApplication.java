package me.hyoseo.housingfinance;

import lombok.RequiredArgsConstructor;
import me.hyoseo.housingfinance.database.model.Institution;
import me.hyoseo.housingfinance.database.model.InstitutionSupport;
import me.hyoseo.housingfinance.database.repository.InstitutionRepository;
import me.hyoseo.housingfinance.database.repository.InstitutionSupportRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;

@RequiredArgsConstructor
@SpringBootApplication
public class HousingFinanceApplication implements CommandLineRunner {

    private final InstitutionRepository institutionRepository;

    private final InstitutionSupportRepository institutionSupportRepository;

    public static void main(String[] args) {
        SpringApplication.run(HousingFinanceApplication.class, args);
    }

    @Override
    public void run(String... args) throws Exception {
        Resource resource = new ClassPathResource("/static/사전과제3.csv");

        List<String> record;
        List<Character> bankNameEndChars = Arrays.asList('1', '(');

        try (Scanner scanner = new Scanner(resource.getFile())) {
            // 첫 번째 행 (은행 이름들 처리)
            if (scanner.hasNextLine()) {
                record = getRecordFromLine(scanner.nextLine());

                record.subList(2, record.size()).stream().map(bankName -> {
                    for (Character bankNameEndChar : bankNameEndChars) {
                        int found_index_pos = bankName.indexOf(bankNameEndChar);
                        if (found_index_pos != -1) {
                            return bankName.substring(0, found_index_pos);
                        }
                    }

                    return bankName;
                }).forEach(bankName -> institutionRepository.save(new Institution(bankName)));
            }

            // 그 외의 행들 (각 금융기관 지원 금액 처리)
            while (scanner.hasNextLine()) {
                record = getRecordFromLine(scanner.nextLine());
                String year = record.get(0);
                String month = record.get(1);

                for (int col = 2; col < record.size(); ++col) {
                    institutionSupportRepository.save(new InstitutionSupport(
                            Short.valueOf(year),
                            Byte.valueOf(month),
                            institutionRepository.findById(col-1).get(),
                            Integer.valueOf(record.get(col))));
                }
            }
        }
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
