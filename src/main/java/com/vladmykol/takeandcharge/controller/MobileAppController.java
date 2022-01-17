package com.vladmykol.takeandcharge.controller;

import com.vladmykol.takeandcharge.conts.EndpointConst;
import com.vladmykol.takeandcharge.monitoring.TelegramNotifierService;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;

@RestController
@RequestMapping(EndpointConst.API_APP)
@RequiredArgsConstructor
public class MobileAppController {
    private final TelegramNotifierService telegramNotifierService;

    @GetMapping("/version")
    public ResponseEntity<String> checkVersion(@RequestParam float currentVersion,
                                               @RequestParam String os) {
        var stableVersion = 2.16f;
        String link = "https://apps.apple.com/us/app/chargebro/id1523146131";
        if ("android".equalsIgnoreCase(os)) {
//            stableVersion = 2.15f;
            link = "https://play.google.com/store/apps/details?id=com.mykovol.takeandcharge";
        }

        if (currentVersion < stableVersion) {
            return ResponseEntity.ok(link);
        }

        return new ResponseEntity<>(link, HttpStatus.PRECONDITION_FAILED);
    }

    @GetMapping("/{id}")
    public void redirectToAppSuccess(HttpServletResponse httpServletResponse,
                                     @Parameter(schema = @Schema(type = "string", allowableValues = {"k05, k13, k15, k20, k14"}, required = true)) @PathVariable String id) {
        final String baseUrl = "https://chargebro.app.link/";
        httpServletResponse.setHeader("Location", baseUrl + id);
        httpServletResponse.setStatus(302);
    }

    @PostMapping("/exception")
    public void exceptionInMobileApp(@RequestParam String exception) {
        telegramNotifierService.errorFromMobileApp(exception);
    }
}
