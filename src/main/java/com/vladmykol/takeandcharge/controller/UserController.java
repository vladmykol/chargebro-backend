package com.vladmykol.takeandcharge.controller;

import com.vladmykol.takeandcharge.conts.EndpointConst;
import com.vladmykol.takeandcharge.dto.AuthenticationResponse;
import com.vladmykol.takeandcharge.dto.SingUpDto;
import com.vladmykol.takeandcharge.dto.SmsRegistrationTokenInfo;
import com.vladmykol.takeandcharge.dto.UserCardDto;
import com.vladmykol.takeandcharge.security.JwtProvider;
import com.vladmykol.takeandcharge.service.RegisterUserService;
import com.vladmykol.takeandcharge.service.UserWalletService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping(EndpointConst.API_USER)
@RequiredArgsConstructor
public class UserController {
    private final UserWalletService userWalletService;

    @GetMapping("/card")
    public List<UserCardDto> getUserCards() {
        final var userCardResp = new ArrayList<UserCardDto>();
        userWalletService.getValidPaymentMethodsOrdered().forEach(userWallet -> {
            final var maskedCard = "**** " + userWallet.getMaskedCard().substring(userWallet.getMaskedCard().length() - 4);
            userCardResp.add(
                    UserCardDto.builder()
                            .id(userWallet.getId())
                            .type(userWallet.getCardType())
                            .maskedNum(maskedCard)
                            .build()
            );
        });
        if (userCardResp.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "no cards assigned to this user");
        }
        return userCardResp;
    }

    @DeleteMapping("/card/{id}")
    public void removeUserCard(@PathVariable String id) {
        if (!userWalletService.removeUserCard(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "card not found");
        }
    }
}
