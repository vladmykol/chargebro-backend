package com.vladmykol.takeandcharge.monitoring;

import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.request.SendMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class TelegramNotifierService {
    private final TelegramBot bot;
    private final String adminChatId;

    public TelegramNotifierService(@Value("${takeandcharge.bot.apikey}") String botToken,
                                   @Value("${takeandcharge.bot.admin.chatid}") String adminChatId) {
        bot = new TelegramBot(botToken);
        this.adminChatId = adminChatId;
    }

    public void messageToAdmin(String text) {
        try {
            bot.execute(new SendMessage(adminChatId, text));
        } catch (Exception e) {
            log.error("Cannot send notification message to admin", e);
        }
    }
}
