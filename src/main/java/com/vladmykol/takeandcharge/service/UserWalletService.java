package com.vladmykol.takeandcharge.service;

import com.vladmykol.takeandcharge.dto.FondyResponse;
import com.vladmykol.takeandcharge.entity.UserWallet;
import com.vladmykol.takeandcharge.exceptions.PaymentException;
import com.vladmykol.takeandcharge.repository.UserWalletRepository;
import com.vladmykol.takeandcharge.utils.SecurityUtil;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.util.List;

@RequiredArgsConstructor
@Service
public class UserWalletService {
    private final UserWalletRepository userWalletRepository;

    public void saveCard(FondyResponse callback) {

        if (callback == null || StringUtils.isBlank(callback.getRectoken())) {
            throw new PaymentException("Card token is missing");
        }

        var userWallet = UserWallet.builder()
                .paymentId(callback.getOrder_id())
                .cardToken(callback.getRectoken())
                .cardType(callback.getCard_type())
                .maskedCard(callback.getMasked_card())
                .build();
        userWalletRepository.save(userWallet);
    }

    public List<UserWallet> getValidPaymentMethodsOrdered() {
        return userWalletRepository.findByUserIdOrderByLastModifiedDateDesc(SecurityUtil.getUser());
    }
}
