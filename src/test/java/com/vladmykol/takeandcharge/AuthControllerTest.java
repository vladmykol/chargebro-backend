package com.vladmykol.takeandcharge;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.vladmykol.takeandcharge.conts.EndpointConst;
import com.vladmykol.takeandcharge.dto.LoginRequest;
import com.vladmykol.takeandcharge.dto.SingUpDto;
import com.vladmykol.takeandcharge.dto.SmsRegistrationTokenInfo;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestPropertySource(locations = "classpath:application-test.properties")
@AutoConfigureMockMvc
class AuthControllerTest {

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
    void notExistingUser() throws Exception {
        LoginRequest loginRequest = LoginRequest.builder()
                .username("testuser")
                .password("testpass").build();

        mvc.perform(post(EndpointConst.API_AUTH + "/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(body(loginRequest)))
                .andExpect(status().isForbidden());
    }

    @Test
    void twoStepSingUp() throws Exception {
        var registerInit = mvc.perform(post(EndpointConst.API_VERSION_1 + EndpointConst.API_AUTH + "/init")
                .contentType(MediaType.APPLICATION_JSON)
                .param("phone", "380000000000"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.validForMin").value(2))
                .andExpect(jsonPath("$.token").isNotEmpty())
                .andReturn();

        String responseBody = registerInit.getResponse().getContentAsString();
        SmsRegistrationTokenInfo registerInitResponse = objectMapper.readValue(responseBody, SmsRegistrationTokenInfo.class);

        SingUpDto singUpDto = SingUpDto.builder()
                .smsCode(registerInitResponse.getCode())
                .token(registerInitResponse.getToken())
                .name("TestUser")
                .password("testpassword")
                .build();

        mvc.perform(post(EndpointConst.API_VERSION_1 + EndpointConst.API_AUTH + "/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(body(singUpDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").isNotEmpty());
    }
}
