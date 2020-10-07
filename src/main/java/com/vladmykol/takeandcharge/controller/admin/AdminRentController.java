package com.vladmykol.takeandcharge.controller.admin;

import com.vladmykol.takeandcharge.dto.RentReportDto;
import com.vladmykol.takeandcharge.entity.Payment;
import com.vladmykol.takeandcharge.service.PaymentService;
import com.vladmykol.takeandcharge.service.RentService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

import static com.vladmykol.takeandcharge.conts.EndpointConst.*;

@RestController
@RequestMapping(API_ADMIN + API_RENT)
@RequiredArgsConstructor
public class AdminRentController {
    private final PaymentService paymentService;
    private final RentService rentService;

    @GetMapping("/payment")
    public List<Payment> getAllPaymentHistory() {
        return paymentService.getAllPaymentHistory();
    }

    @GetMapping("/report")
    public List<RentReportDto> getRentReport() {
        return rentService.getRentReport();
    }

    @DeleteMapping("/clear")
    public void rentClear() {
        rentService.clearRent();
    }
}
