package me.hyoseo.housingfinance.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import me.hyoseo.housingfinance.database.model.User;
import me.hyoseo.housingfinance.database.repository.UserRepository;
import me.hyoseo.housingfinance.request.IdPassword;
import me.hyoseo.housingfinance.service.CryptoService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import java.util.Optional;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureMockMvc
public class AuthControllerTest {

    @MockBean
    CryptoService cryptoService;

    @MockBean
    UserRepository userRepository;

    @Autowired
    MockMvc mockMvc;

    private final String testToken = "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9." +
            "eyJpc3MiOiJoeW9zZW8iLCJ1aWQiOiJoeW9zZW8iLCJpYXQiOjE1ODAwNDQwNTcsImV4cCI6MTU4MDA0NzY1N30." +
            "jGP4gjX6oR-IA5IyQk9fgI1xGV6pugBXshsYnd7TF8g";

    @Test
    public void signUp() throws Exception {
        when(cryptoService.createToken("hyoseo")).thenReturn(testToken);

        ObjectMapper objectMapper = new ObjectMapper();

        mockMvc.perform(post("/auth/signup")
                .content(objectMapper.writeValueAsString(new IdPassword("hyoseo", "abcd12#")))
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.jsonPath("$.access_token").value(testToken)).andDo(print());
    }

    @Test
    public void signIn() throws Exception {
        String encPassword = "M2UXALZG42LbaRhtnYZ6Lz+zQezUeCWM7FsVXmBOd64=";
        when(cryptoService.encrypt("hyoseo", "abcd12#")).thenReturn(encPassword);
        when(userRepository.findById("hyoseo")).thenReturn(Optional.of(new User("hyoseo", encPassword)));
        when(cryptoService.createToken("hyoseo")).thenReturn(testToken);

        ObjectMapper objectMapper = new ObjectMapper();

        mockMvc.perform(post("/auth/signin")
                .content(objectMapper.writeValueAsString(new IdPassword("hyoseo", "abcd12#")))
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.jsonPath("$.access_token").value(testToken)).andDo(print());
    }

    @Test
    public void refresh() throws Exception {
        when(cryptoService.parse(testToken)).thenReturn("hyoseo");
        when(cryptoService.createToken("hyoseo")).thenReturn(testToken);

        mockMvc.perform(post("/auth/refresh")
                .header("Access-Token", testToken)
                .header("Authorization", "Bearer Token")
                .requestAttr("user_id", "hyoseo")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.jsonPath("$.access_token").value(testToken)).andDo(print());
    }
}