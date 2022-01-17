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

        assertThat(paymentService.getRentPriceAmount(TimeUnit.HOURS.toMillis(1)), is(1800));

        assertThat(paymentService.getRentPriceAmount(TimeUnit.HOURS.toMillis(1) + 1), is(1800));

        assertThat(paymentService.getRentPriceAmount(TimeUnit.HOURS.toMillis(2)), is(3600));

        assertThat(paymentService.getRentPriceAmount(TimeUnit.HOURS.toMillis(2) + TimeUnit.MINUTES.toMillis(30)), is(4900));

        assertThat(paymentService.getRentPriceAmount(TimeUnit.HOURS.toMillis(23)), is(4900));

//        assertThat(paymentService.getRentPriceAmount(TimeUnit.HOURS.toMillis(24)), is(5800));

        assertThat(paymentService.getRentPriceAmount(TimeUnit.HOURS.toMillis(25)), is(9800));
    }

}
