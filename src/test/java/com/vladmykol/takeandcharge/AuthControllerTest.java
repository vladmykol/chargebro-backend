package com.vladmykol.takeandcharge;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.vladmykol.takeandcharge.conts.EndpointConst;
import com.vladmykol.takeandcharge.conts.RoleEnum;
import com.vladmykol.takeandcharge.dto.LoginRequest;
import com.vladmykol.takeandcharge.dto.SingUpDto;
import com.vladmykol.takeandcharge.dto.SmsRegistrationTokenInfo;
import com.vladmykol.takeandcharge.entity.Role;
import com.vladmykol.takeandcharge.entity.User;
import com.vladmykol.takeandcharge.service.RegisterUserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collections;

import static com.vladmykol.takeandcharge.conts.EndpointConst.API_AUTH;
import static com.vladmykol.takeandcharge.entity.User.UserStatus.REGISTERED;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestPropertySource(
        locations = "classpath:application-test.properties")
@AutoConfigureMockMvc
//@RestClientTest
class AuthControllerTest {
//    @Autowired
//    private RegisterUserService registerUserService;
//    @Autowired
//    private PasswordEncoder passwordEncoder;

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
                .username("Admin")
                .password("Admin").build();

        mvc.perform(post(EndpointConst.API_AUTH + "/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(body(loginRequest)))
                .andExpect(status().isForbidden());

    }


    @Test
    void twoStepSingUp() throws Exception {
        var registerInit = mvc.perform(post(EndpointConst.API_VERSION_1 + EndpointConst.API_AUTH + "/init")
                .contentType(MediaType.APPLICATION_JSON)
                .param("phone", "3809312312345"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.validForMin").value(2))
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

        mvc.perform(post(EndpointConst.API_VERSION_1 + EndpointConst.API_AUTH + "/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(body(singUpDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").isNotEmpty());
    }

//    @Test
//    public void newUser() throws Exception {
//        var role = new Role();
//        role.setRole(RoleEnum.ADMIN);
//
//        var userDto = User.builder()
//                .userName("Vlad")
//                .password(passwordEncoder.encode("1234"))
//                .roles(Collections.singleton(role))
//                .bonusAmount(0)
//                .userStatus(REGISTERED)
//                .build();
//
//        registerUserService.saveUser(userDto);
//    }


}
