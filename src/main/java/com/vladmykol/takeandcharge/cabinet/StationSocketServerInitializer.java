package com.vladmykol.takeandcharge.cabinet;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class StationSocketServerInitializer {

    private final StationSocketServer server;

    @EventListener(ApplicationReadyEvent.class)
    public void startupServer() throws IOException {
        server.start();
    }
}
