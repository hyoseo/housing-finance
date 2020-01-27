package me.hyoseo.housingfinance.controller;

import me.hyoseo.housingfinance.service.CryptoService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureMockMvc
public class InstituteControllerTest {

    @MockBean
    CryptoService cryptoService;

    @Autowired
    MockMvc mockMvc;

    private final String testToken = "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9." +
            "eyJpc3MiOiJoeW9zZW8iLCJ1aWQiOiJoeW9zZW8iLCJpYXQiOjE1ODAwNDQwNTcsImV4cCI6MTU4MDA0NzY1N30." +
            "jGP4gjX6oR-IA5IyQk9fgI1xGV6pugBXshsYnd7TF8g";

    @Before
    public void setUp() throws Exception {
        when(cryptoService.parse(testToken)).thenReturn("hyoseo");
        mockMvc.perform(post("/data/csv_files").header("Access-Token", testToken));
    }

    @Test
    public void getInstitutes() throws Exception {
        mockMvc.perform(get("/institutes")
                .header("Access-Token", testToken)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.[*].name",
                        hasItems("주택도시기금", "국민은행", "우리은행", "신한은행", "한국시티은행",
                                "하나은행", "농협은행/수협은행", "외환은행", "기타은행")))
                .andDo(print());
    }

    @Test
    public void getTotalInstitutesSupport() throws Exception {
        mockMvc.perform(get("/institutes/total_institutes_support")
                .header("Access-Token", testToken)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.name").value("주택금융 공급현황"))
                .andExpect(jsonPath("$.yearly_institutes_supports.[*].year",
                        contains(2005, 2006, 2007, 2008, 2009, 2010, 2011, 2012, 2013, 2014, 2015, 2016, 2017)))
                .andExpect(jsonPath("$.yearly_institutes_supports.[*].total_amount",
                        contains(48016, 41210, 50893, 67603, 96545, 114903, 206693, 275591, 265805, 318771, 374773,
                                400971, 295126)))
                .andDo(print());
    }

    @Test
    public void getTopInstituteSupport() throws Exception {
        mockMvc.perform(get("/institutes/top_institute_support")
                .header("Access-Token", testToken)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.year").value(2014))
                .andExpect(jsonPath("$.bank").value("주택도시기금"))
                .andDo(print());
    }

    @Test
    public void getInstituteAvgMinMaxSupport() throws Exception {
        mockMvc.perform(get("/institutes/institute_avg_min_max_support")
                .header("Access-Token", testToken)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.bank").value("외환은행"))
                .andExpect(jsonPath("$.support_amount.[*].year", contains(2017, 2015)))
                .andExpect(jsonPath("$.support_amount.[*].amount", contains(0, 1702)))
                .andDo(print());
    }
}