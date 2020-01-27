package me.hyoseo.housingfinance.service;


import me.hyoseo.housingfinance.error.CommonException;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = {CryptoService.class})
public class CryptoServiceTest {

    @Autowired
    private CryptoService cryptoService;

    @Test
    public void encrypt() {
        String encPassword = cryptoService.encrypt("hyoseo", "abcd12#");
        assertThat(encPassword).isEqualTo("M2UXALZG42LbaRhtnYZ6Lz+zQezUeCWM7FsVXmBOd64=");
    }

    @Rule
    public ExpectedException exceptionRule = ExpectedException.none();

    @Test
    public void parse() {
        exceptionRule.expect(CommonException.class);
        exceptionRule.expectMessage("JWT expired at");

        String testToken = "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9." +
                "eyJpc3MiOiJoeW9zZW8iLCJ1aWQiOiJoeW9zZW8iLCJpYXQiOjE1ODAwNDQwNTcsImV4cCI6MTU4MDA0NzY1N30." +
                "jGP4gjX6oR-IA5IyQk9fgI1xGV6pugBXshsYnd7TF8g";
        cryptoService.parse(testToken);
    }
}