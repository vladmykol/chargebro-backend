package com.vladmykol.takeandcharge.controller.admin;

import com.vladmykol.takeandcharge.dto.HoldDetails;
import com.vladmykol.takeandcharge.dto.RentReportDto;
import com.vladmykol.takeandcharge.entity.Payment;
import com.vladmykol.takeandcharge.service.PaymentService;
import com.vladmykol.takeandcharge.service.RentService;
import com.vladmykol.takeandcharge.service.UserService;
import com.vladmykol.takeandcharge.utils.SecurityUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static com.vladmykol.takeandcharge.conts.EndpointConst.API_ADMIN;
import static com.vladmykol.takeandcharge.conts.EndpointConst.API_RENT;

@RestController
@RequestMapping(API_ADMIN + API_RENT)
@RequiredArgsConstructor
public class AdminRentController {
    private final PaymentService paymentService;
    private final RentService rentService;
    private final UserService userService;

    @GetMapping("/payment")
    public List<Payment> getAllPaymentHistory() {
        return paymentService.getAllPaymentHistory();
    }

    @GetMapping("/report")
    public List<RentReportDto> getRentReport() {
//        HttpServletRequest request
//        Locale clientLocale = request.getLocale();
//        Calendar calendar = Calendar.getInstance(clientLocale);
//        TimeZone clientTimeZone = calendar.getTimeZone();
//        System.out.println(clientTimeZone);
        return rentService.getRentReport();
    }

    @DeleteMapping("/clear")
    public void rentClear(@RequestParam String orderId) {
        if (orderId == null || orderId.isEmpty()) {
//            rentService.clearRent();
        } else {
            rentService.clearRentRow(orderId);
        }
    }

    @PutMapping("/hold")
    public Payment HoldMoney(@RequestParam String userId, @RequestParam int amount, @RequestParam String rentId) {
        final var userPhone = userService.getUserPhone(SecurityUtil.getUser());
        final var holdDetails = HoldDetails.builder()
                .userId(userId)
                .amount(amount)
                .rentId(rentId)
                .powerBankId("Manual hold")
                .userPhone(userPhone)
                .isPreAuth(true)
                .build();
        return paymentService.holdMoney(holdDetails);
    }
}
