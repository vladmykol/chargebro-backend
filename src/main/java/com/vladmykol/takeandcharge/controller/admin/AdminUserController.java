package com.vladmykol.takeandcharge.controller.admin;

import com.vladmykol.takeandcharge.entity.User;
import com.vladmykol.takeandcharge.service.RegisterUserService;
import com.vladmykol.takeandcharge.service.SmsService;
import com.vladmykol.takeandcharge.service.WebSocketServer;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;
import java.util.Map;

import static com.vladmykol.takeandcharge.conts.EndpointConst.API_ADMIN;
import static com.vladmykol.takeandcharge.conts.EndpointConst.API_USER;

@RestController
@RequestMapping(API_ADMIN + API_USER)
@RequiredArgsConstructor
public class AdminUserController {
    private final RegisterUserService registerUserService;
    private final WebSocketServer webSocketServer;
    private final SmsService smsService;

    @PostMapping()
    public void saveUser(@Valid @RequestBody User user) {
        registerUserService.saveUser(user);
    }

    @DeleteMapping()
    public void deleteUser(@RequestParam String id) {
        registerUserService.deleteUser(id);
    }

    @GetMapping()
    public List<User> findAll() {
        return registerUserService.findAll();
    }

    @GetMapping("/online-client")
    public Map<String, List<String>> findAllConnectedWebSocketClients() {
        return webSocketServer.getConnectedClients();
    }

    @PostMapping("/notify")
    @Operation(description = "For manual SMS sending to user")
    public void sendSms(@RequestParam String phone,
                        @RequestParam(defaultValue = "Ми помітили, що Ви вчора не змогли скористатися нашим сервісом. Вибачте за незручності, ми все полагодили! До нових зарядів!") String text) {
        smsService.sendSMS(text, phone);
    }


}
