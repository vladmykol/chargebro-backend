package com.vladmykol.takeandcharge;

import com.vladmykol.takeandcharge.service.PaymentService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

import java.util.concurrent.TimeUnit;

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

        assertThat(paymentService.getRentPriceAmount(TimeUnit.MINUTES.toMillis(29)), is(0));

        assertThat(paymentService.getRentPriceAmount(TimeUnit.MINUTES.toMillis(30)), is(900));

        assertThat(paymentService.getRentPriceAmount(TimeUnit.MINUTES.toMillis(30) + 1), is(900));

        assertThat(paymentService.getRentPriceAmount(TimeUnit.MINUTES.toMillis(60)), is(1800));

        assertThat(paymentService.getRentPriceAmount(TimeUnit.MINUTES.toMillis(60) + 1), is(1800));

        assertThat(paymentService.getRentPriceAmount(TimeUnit.MINUTES.toMillis(89)), is(1800));

        assertThat(paymentService.getRentPriceAmount(TimeUnit.MINUTES.toMillis(91)), is(2700));
    }

}
