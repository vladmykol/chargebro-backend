package com.vladmykol.takeandcharge;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.vladmykol.takeandcharge.dto.UserDto;
import com.vladmykol.takeandcharge.dto.LoginRequest;
import com.vladmykol.takeandcharge.entity.Station;
import com.vladmykol.takeandcharge.repository.StationRepository;
import com.vladmykol.takeandcharge.security.TokenService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.geo.Point;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import java.io.IOException;

import static com.vladmykol.takeandcharge.conts.EndpointConst.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(
        locations = "classpath:application-test.properties")
//@DataMongoTest
//@RestClientTest
class ApiTest {
    @Autowired
    private StationRepository stationRepository;
    @Autowired
    private TokenService tokenService;

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
    void shouldHandleCoupleClients() throws IOException {
        Station station = Station.builder()
                .id("STWA312001000005")
                .location(new Point(50.481952, 30.412420))
                .build();
        stationRepository.save(station);
    }

    @Test
    void singUp() throws Exception {
        UserDto userDto = UserDto.builder()
                .firstName("Vlad")
                .lastName("Drunkula")
                .password("1111")
                .build();

        mvc.perform(post(API_AUTH + API_AUTH_SINGUP)
                .contentType(MediaType.APPLICATION_JSON)
                .content(body(userDto)))
                .andExpect(status().isOk());


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
    void getLocation() throws Exception {
        String token = tokenService.generateJwtToken("5eb3e5279ef2620da98e0a611");

        mvc.perform(get("/rent/location")
                .header("Authorization", token)
                .param("x", "50.480225")
                .param("y", "30.415499"))
                .andDo(print())
                .andExpect(status().isOk());
    }


}
