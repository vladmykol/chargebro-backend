package com.vladmykol.takeandcharge;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.vladmykol.takeandcharge.dto.LoginRequest;
import com.vladmykol.takeandcharge.dto.SingUpDto;
import com.vladmykol.takeandcharge.dto.SmsRegistrationTokenInfo;
import com.vladmykol.takeandcharge.repository.StationRepository;
import com.vladmykol.takeandcharge.security.JwtProvider;
import com.vladmykol.takeandcharge.service.SmsService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import static com.vladmykol.takeandcharge.conts.EndpointConst.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestPropertySource(
        locations = "classpath:application-test.properties")
@AutoConfigureMockMvc
//@RestClientTest
class AuthControllerTest {
    @Autowired
    private StationRepository stationRepository;
    @Autowired
    private JwtProvider jwtProvider;
    @Autowired
    private SmsService smsService;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private MockMvc mvc;

    public static String body(final Object obj) {
        try {
            final ObjectMapper mapper = new ObjectMapper();
            return mapper.writeValueAsString(obj);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }


    @Test
    void logIn() throws Exception {
        LoginRequest loginRequest = LoginRequest.builder()
                .username("Admin")
                .password("Admin").build();

        mvc.perform(post(API_AUTH + API_AUTH_LOGIN)
                .contentType(MediaType.APPLICATION_JSON)
                .content(body(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").isNotEmpty());

    }


    @Test
    void twoStepSingUp() throws Exception {
        var registerInit = mvc.perform(post(API_AUTH + API_AUTH_REGISTER_INIT)
                .contentType(MediaType.APPLICATION_JSON)
                .param("phone", "380939008021"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.validForMin").value(10))
                .andExpect(jsonPath("$.token").isNotEmpty())
                .andReturn();

        String responseBody = registerInit.getResponse().getContentAsString();
        SmsRegistrationTokenInfo registerInitResponse = objectMapper.readValue(responseBody, SmsRegistrationTokenInfo.class);

        SingUpDto singUpDto = SingUpDto.builder()
                .smsCode(registerInitResponse.getCode())
                .token(registerInitResponse.getToken())
                .name("380939008020")
                .password("1111")
                .build();

        mvc.perform(post(API_AUTH + API_AUTH_REGISTER)
                .contentType(MediaType.APPLICATION_JSON)
                .content(body(singUpDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").isNotEmpty());
    }


}
