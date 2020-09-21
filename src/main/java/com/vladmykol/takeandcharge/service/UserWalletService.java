package com.vladmykol.takeandcharge.service;

import com.vladmykol.takeandcharge.dto.FondyResponse;
import com.vladmykol.takeandcharge.entity.UserWallet;
import com.vladmykol.takeandcharge.repository.UserWalletRepository;
import com.vladmykol.takeandcharge.utils.SecurityUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@RequiredArgsConstructor
@Service
public class UserWalletService {
    private final UserWalletRepository userWalletRepository;

    public void saveCard(String paymentId, FondyResponse callbackDto) {
        if (callbackDto.getRectoken() == null) {
            throw new RuntimeException("Card token is missing");
        }

        var userWallet = UserWallet.builder()
                .paymentId(paymentId)
                .cardToken(callbackDto.getRectoken())
                .cardType(callbackDto.getCard_type())
                .maskedCard(callbackDto.getMasked_card())
                .build();
        userWalletRepository.save(userWallet);
    }

    public List<UserWallet> getValidPaymentMethodsOrdered() {
        return userWalletRepository.findByUserIdOrderByLastModifiedDateDesc(SecurityUtil.getUser());
    }
}
