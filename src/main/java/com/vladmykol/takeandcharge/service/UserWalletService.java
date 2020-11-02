package com.vladmykol.takeandcharge.service;

import com.vladmykol.takeandcharge.dto.FondyResponse;
import com.vladmykol.takeandcharge.entity.UserWallet;
import com.vladmykol.takeandcharge.exceptions.PaymentException;
import com.vladmykol.takeandcharge.repository.UserWalletRepository;
import com.vladmykol.takeandcharge.utils.SecurityUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.util.List;

@RequiredArgsConstructor
@Service
@Slf4j
public class UserWalletService {
    private final UserWalletRepository userWalletRepository;
    private final RentService rentService;

    public void saveCard(FondyResponse callback) {

        if (callback == null || StringUtils.isBlank(callback.getRectoken())) {
            throw new PaymentException("Card token is missing");
        }

        final var cardExists = userWalletRepository.existsByCardTokenAndUserIdAndIsRemovedFalse(callback.getRectoken(), SecurityUtil.getUser());
        if (!cardExists) {
            var userWallet = UserWallet.builder()
                    .paymentId(callback.getOrder_id())
                    .cardToken(callback.getRectoken())
                    .cardType(callback.getCard_type())
                    .maskedCard(callback.getMasked_card())
                    .build();
            userWalletRepository.save(userWallet);
        } else {
            log.warn("Card already exist {}", callback.getMasked_card());
        }
    }

    public List<UserWallet> getValidPaymentMethodsOrdered() {
        return userWalletRepository.findByUserIdAndIsRemovedFalseOrderByLastModifiedDateDesc(SecurityUtil.getUser());
    }

    public boolean removeUserCard(String id) {
        final var optionalUserWallet = userWalletRepository.findById(id);
        if (optionalUserWallet.isPresent()) {
            if (rentService.isUserHasActiveRent()) {
                throw new PaymentException("Cannot delete card while rent is still in progress");
            } else {
                optionalUserWallet.get().setRemoved(true);
                userWalletRepository.save(optionalUserWallet.get());
            }
        }
        return optionalUserWallet.isPresent();
    }

    public boolean isUserHasPaymentMethod(String user) {
        return userWalletRepository.existsByUserIdAndIsRemovedFalse(user);
    }
}
