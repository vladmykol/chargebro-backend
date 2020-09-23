package com.vladmykol.takeandcharge.controller;

import com.vladmykol.takeandcharge.conts.EndpointConst;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletResponse;

@RestController
@RequestMapping(EndpointConst.API_APP)
@RequiredArgsConstructor
public class AppRedirectController {

    @GetMapping("/success")
    public void redirectToAppSuccess(HttpServletResponse httpServletResponse) {
        httpServletResponse.setHeader("Location", "takeandcharge://home");
        httpServletResponse.setStatus(302);
    }

    @GetMapping("/error")
    public void redirectToAppError(HttpServletResponse httpServletResponse) {
        httpServletResponse.setHeader("Location", "takeandcharge://error");
        httpServletResponse.setStatus(302);
    }

}
