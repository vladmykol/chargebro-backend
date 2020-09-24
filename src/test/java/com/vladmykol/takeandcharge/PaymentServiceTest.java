package com.vladmykol.takeandcharge;

import com.vladmykol.takeandcharge.service.PaymentService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestPropertySource(
        locations = "classpath:application-test.properties")
@AutoConfigureMockMvc
class PaymentServiceTest {
    @Autowired
    private PaymentService paymentService;

    @Test
    void checkRentPrice() throws Exception {
        assertThat(paymentService.getRentPriceAmount(2 * 60 * 1000), is(0));

        assertThat(paymentService.getRentPriceAmount(5 * 60 * 1000), is(0));

        assertThat(paymentService.getRentPriceAmount(5 * 60 * 1000 + 1), is(100));

        assertThat(paymentService.getRentPriceAmount(6 * 60 * 1000), is(100));

        assertThat(paymentService.getRentPriceAmount(6 * 60 * 1000 + 999), is(200));

        assertThat(paymentService.getRentPriceAmount(7 * 60 * 1000 + 1), is(300));
    }

}
