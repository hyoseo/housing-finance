package me.hyoseo.housingfinance.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.Pattern;

@Getter
@Setter
@AllArgsConstructor
public class IdPassword {
    @Pattern(regexp = "^[a-zA-Z][a-zA-Z0-9]{5,}$",
            message = "아이디는 5자리 이상으로 영문자와 숫자의 조합으로 입력해주세요. 첫 글자는 영문자여야 합니다.")
    private String Id;

    @Pattern(regexp = "(?=.*\\d{1,50})(?=.*[~`!@#$%\\^&*()-+=]{1,50})(?=.*[a-zA-Z]{2,50}).{6,50}$",
            message = "비밀번호는 숫자, 특문 각 1회 이상, 영문은 2개 이상 사용하여 6자리 이상 입력해 주세요.")
    private String password;
}
