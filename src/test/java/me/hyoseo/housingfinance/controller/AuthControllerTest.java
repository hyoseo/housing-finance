package me.hyoseo.housingfinance.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jayway.jsonpath.JsonPath;
import io.jsonwebtoken.ExpiredJwtException;
import me.hyoseo.housingfinance.database.repository.UserRepository;
import me.hyoseo.housingfinance.request.IdPassword;
import me.hyoseo.housingfinance.service.CryptoService;
import org.hibernate.exception.ConstraintViolationException;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureMockMvc
public class AuthControllerTest {

    @Autowired
    CryptoService cryptoService;

    @Autowired
    UserRepository userRepository;

    @Autowired
    MockMvc mockMvc;

    @Test
    public void signUp() throws Exception {
        requestSignUp("hyoseo1", "abcd12#");
    }

    public String requestSignUp(String id, String password) throws Exception {
        assertThat(userRepository.existsById(id)).isEqualTo(false);

        ObjectMapper objectMapper = new ObjectMapper();

        MvcResult mvcSignUpResult = mockMvc.perform(post("/auth/signup")
                .content(objectMapper.writeValueAsString(new IdPassword(id, password)))
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andDo(print())
                .andReturn();

        String singUpAccessToken = JsonPath.read(mvcSignUpResult.getResponse().getContentAsString(), "$.access_token");
        assertThat(cryptoService.parse(singUpAccessToken)).isEqualTo(id);

        return singUpAccessToken;
    }

    @Test
    public void signUpWithSameId() {
        AtomicBoolean isTheIdUsed = new AtomicBoolean(false);

        CompletableFuture.allOf(
                requestSignUpWithSameId("hyoseo2", "abcde12#", isTheIdUsed),
                requestSignUpWithSameId("hyoseo2", "fjksl12#", isTheIdUsed),
                requestSignUpWithSameId("hyoseo2", "vnxcm42$", isTheIdUsed),
                requestSignUpWithSameId("hyoseo2", "uiroe12&", isTheIdUsed),
                requestSignUpWithSameId("hyoseo2", "safoe12&", isTheIdUsed),
                requestSignUpWithSameId("hyoseo2", "uvxce12&", isTheIdUsed),
                requestSignUpWithSameId("hyoseo2", "rtvbe12&", isTheIdUsed),
                requestSignUpWithSameId("hyoseo2", "nmccc12#", isTheIdUsed))
                .join();

        assertThat(isTheIdUsed.get()).isEqualTo(true);
    }

    public CompletableFuture<String> requestSignUpWithSameId(String id, String password, AtomicBoolean isTheIdUsed) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                ObjectMapper objectMapper = new ObjectMapper();

                MvcResult mvcResult = mockMvc.perform(post("/auth/signup")
                        .content(objectMapper.writeValueAsString(new IdPassword(id, password)))
                        .contentType(MediaType.APPLICATION_JSON))
                        .andDo(print())
                        .andReturn();

                if (mvcResult.getResponse().getStatus() == HttpStatus.OK.value()) {
                    assertThat(isTheIdUsed.compareAndSet(false, true)).isEqualTo(true);
                    String accessToken = JsonPath.read(mvcResult.getResponse().getContentAsString(), "$.access_token");
                    assertThat(cryptoService.parse(accessToken)).isEqualTo(id);
                } else {
                    assertThat(mvcResult.getResponse().getStatus()).isEqualTo(HttpStatus.FORBIDDEN.value());
                    if (mvcResult.getResolvedException().getCause() != null)
                        assertThat(mvcResult.getResolvedException().getCause().getCause())
                                .isOfAnyClassIn(DataIntegrityViolationException.class, ConstraintViolationException.class);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        });
    }

    @Test
    public void signIn() throws Exception {
        String singUpAccessToken = requestSignUp("hyoseo3", "abcd12#");

        Thread.sleep(1000);

        ObjectMapper objectMapper = new ObjectMapper();

        mockMvc.perform(post("/auth/signin")
                .content(objectMapper.writeValueAsString(new IdPassword("hyoseo3", "abcd12#")))
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andDo(mvcResult -> {
                    String signInAccessToken = JsonPath.read(
                            mvcResult.getResponse().getContentAsString(),
                            "$.access_token");
                    assertThat(cryptoService.parse(signInAccessToken)).isEqualTo("hyoseo3");
                    assertThat(signInAccessToken).isNotEqualTo(singUpAccessToken);
                })
                .andDo(print());
    }

    @Test
    public void refresh() throws Exception {
        String singUpAccessToken = requestSignUp("hyoseo4", "abcd12#");

        Thread.sleep(1000);

        mockMvc.perform(post("/auth/refresh")
                .header("Access-Token", singUpAccessToken)
                .header("Authorization", "Bearer Token")
                .requestAttr("user_id", "hyoseo4")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andDo(mvcResult -> {
                    String refreshAccessToken = JsonPath.read(
                            mvcResult.getResponse().getContentAsString(),
                            "$.access_token");
                    assertThat(cryptoService.parse(refreshAccessToken)).isEqualTo("hyoseo4");
                    assertThat(refreshAccessToken).isNotEqualTo(singUpAccessToken);
                })
                .andDo(print());
    }

    @Test
    public void refreshWithExpiredToken() throws Exception {
        final String expiredToken = "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9." +
                "eyJpc3MiOiJoeW9zZW8iLCJ1aWQiOiJoeW9zZW8iLCJpYXQiOjE1ODAwNDQwNTcsImV4cCI6MTU4MDA0NzY1N30." +
                "jGP4gjX6oR-IA5IyQk9fgI1xGV6pugBXshsYnd7TF8g";

        mockMvc.perform(post("/auth/refresh")
                .header("Access-Token", expiredToken)
                .header("Authorization", "Bearer Token")
                .requestAttr("user_id", "hyoseo")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized())
                .andDo(mvcResult -> assertThat(mvcResult.getResolvedException())
                        .hasCauseExactlyInstanceOf(ExpiredJwtException.class))
                .andDo(print());
    }
}