package com.vladmykol.takeandcharge.controller;

import com.vladmykol.takeandcharge.conts.EndpointConst;
import io.swagger.annotations.ApiParam;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;

@RestController
@RequestMapping(EndpointConst.API_APP)
@RequiredArgsConstructor
public class MobileAppController {

    @GetMapping("/version")
    public ResponseEntity<String> checkVersion(@RequestParam float currentVersion,
                                               @RequestParam String os) {
        final var stableVersion = 1.26;

        String link = "https://apps.apple.com/us/app/chargebro/id1523146131";
        if ("android".equalsIgnoreCase(os)) {
            link = "https://play.google.com/store/apps/details?id=com.mykovol.takeandcharge";
        }

        if (currentVersion < stableVersion) {
            return ResponseEntity.ok(link);
        } else {
            return new ResponseEntity<>(link, HttpStatus.PRECONDITION_FAILED);
        }
    }

    @GetMapping("/{id}")
    public void redirectToAppSuccess(HttpServletResponse httpServletResponse,
                                     @ApiParam(allowableValues = "K005, K013, K015", required = true) @PathVariable String id) {
        final String baseUrl = "https://take-and-charge.app.link/";
        httpServletResponse.setHeader("Location", baseUrl + id);
        httpServletResponse.setStatus(302);
    }
}
