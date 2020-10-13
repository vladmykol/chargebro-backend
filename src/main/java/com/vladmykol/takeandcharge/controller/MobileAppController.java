package com.vladmykol.takeandcharge.controller;

import com.vladmykol.takeandcharge.conts.EndpointConst;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletResponse;

@RestController
@RequestMapping(EndpointConst.API_APP)
@RequiredArgsConstructor
public class MobileAppController {

    @GetMapping("/version")
    public ResponseEntity<String> checkVersion(@RequestParam float currentVersion,
                                               @RequestParam String os) {
        String link = "";
        if ("android".equalsIgnoreCase(os)) {
            link = "https://play.google.com/store/apps/details?id=com.mykovol.takeandcharge";
        } else if ("ios".equalsIgnoreCase(os)) {
            link = "https://testflight.apple.com/join/jmqPENN8";
        }
        System.out.println("Current app version " + currentVersion);
        if (currentVersion < 0.34) {
            return ResponseEntity.ok(link);
        } else {
            return new ResponseEntity<>(link, HttpStatus.PRECONDITION_FAILED);
        }
    }

    @GetMapping()
    public void redirectToAppSuccess(HttpServletResponse httpServletResponse) {
//        httpServletResponse.setHeader("Location", "chargebro://home");
        httpServletResponse.setHeader("Location", "https://take-and-charge.app.link/CB0001");
        httpServletResponse.setStatus(302);
    }
}
