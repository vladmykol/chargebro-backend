package com.vladmykol.takeandcharge;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.vladmykol.takeandcharge.repository.StationRepository;
import com.vladmykol.takeandcharge.security.TokenService;
import com.vladmykol.takeandcharge.service.SmsService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;

import static com.vladmykol.takeandcharge.conts.EndpointConst.API_RENT;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@WebAppConfiguration
@AutoConfigureMockMvc
@TestPropertySource(
        locations = "classpath:application-test.properties")
//@DataMongoTest
//@RestClientTest
class RentControllerTest {
    @Autowired
    private StationRepository stationRepository;
    @Autowired
    private TokenService tokenService;
    @Autowired
    private SmsService smsService;

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
    void rent() throws Exception {
        String token = tokenService.generateAuthToken("5eb3e5279ef2620da98e0a611");

        mvc.perform(post(API_RENT)
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", token + "1")
                .param("stationId", "STW"))
                .andExpect(status().isOk());

    }


    @Test
    void getLocation() throws Exception {
        String token = tokenService.generateAuthToken("5eb3e5279ef2620da98e0a611");

        mvc.perform(get("/rent/location")
                .header("Authorization", token)
                .param("x", "50.480225")
                .param("y", "30.415499"))
                .andDo(print())
                .andExpect(status().isOk());
    }

    @Test
    void saveTemp() throws Exception {
        smsService.checkIfSmsSend("e23d9572-5f23-a2b5-a024-82de067cbcb7");
    }

}
