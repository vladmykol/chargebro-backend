package com.vladmykol.takeandcharge.service;

import com.vladmykol.takeandcharge.entity.LiqPayHistory;
import com.vladmykol.takeandcharge.entity.User;
import com.vladmykol.takeandcharge.entity.UserWallet;
import com.vladmykol.takeandcharge.repository.LiqPayHistoryRepository;
import com.vladmykol.takeandcharge.repository.UserRepository;
import com.vladmykol.takeandcharge.repository.UserWalletRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentService {
    private final LiqPayHistoryRepository liqPayHistoryRepository;
    private final UserWalletRepository userWalletRepository;
    private final UserRepository userRepository;

    public void savePaymentCallback(LiqPayHistory liqPayHistory) {
        var savedLiqPayHistory = liqPayHistoryRepository.save(liqPayHistory);

        if ("auth".equalsIgnoreCase(savedLiqPayHistory.getAction())) {
            Optional<User> user = userRepository.findById(savedLiqPayHistory.getCustomer());
            if (user.isPresent()) {
                var userWallet = UserWallet.builder()
                        .userId(user.get().getId())
                        .card_token(savedLiqPayHistory.getCard_token())
                        .liqPayHistory(savedLiqPayHistory)
                        .build();
                userWalletRepository.save(userWallet);
            }
        }
    }

    public List<LiqPayHistory> getAllPaymentHistory() {
        return liqPayHistoryRepository.findAll();
    }
}
