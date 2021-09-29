package com.vladmykol.takeandcharge.monitoring;

import com.vladmykol.takeandcharge.entity.Rent;
import com.vladmykol.takeandcharge.entity.Station;
import com.vladmykol.takeandcharge.service.StationServiceHelper;
import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.model.request.ParseMode;
import com.pengrad.telegrambot.request.SendMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class TelegramNotifierService {
    @Value("${takeandcharge.bot.apikey}")
    private TelegramBot bot;
    @Value("${takeandcharge.bot.admin.chatid}")
    private String adminChatId;
    private final StationServiceHelper stationServiceHelper;

//    public TelegramNotifierService(@Value("${takeandcharge.bot.apikey}") String botToken,
//                                   @Value("${takeandcharge.bot.admin.chatid}") String adminChatId) {
//        bot = new TelegramBot(botToken);
//        this.adminChatId = adminChatId;
//    }

    private void messageToAdmin(String text) {
        try {
            SendMessage sendMessage = new SendMessage(adminChatId, text);
            sendMessage.parseMode(ParseMode.HTML);
            bot.execute(sendMessage);
        } catch (Exception e) {
            log.error("Cannot send notification message to admin with text = " + text, e);
        }
    }

    private String stationInfo(Station station) {
        return "\n--\n<code>Place</code>: " + station.getPlaceName() +
                "\n<code>SIM</code>: " + station.getSimPhoneNumber();
    }

    private String stationName(Station station) {
        return "Station <b>" + station.getShortId() + "</b> ";
    }

    public void stationConnected(String cabinetId) {
        var station = stationServiceHelper.getByIdOrNew(cabinetId);
        String msg = stationName(station) + "is reconnected" + stationInfo(station);
        messageToAdmin(msg);
    }

    public void backOnline(String cabinetId) {
        var station = stationServiceHelper.getByIdOrNew(cabinetId);
        String msg = "\uD83D\uDE05 " + stationName(station) + "is back online" + stationInfo(station);
        messageToAdmin(msg);
    }

    public void wentOffline(Station station) {
        String msg = "\uD83D\uDE14 " + stationName(station) + "went offline" + stationInfo(station);
        messageToAdmin(msg);
    }


    public void rentFinished(Rent rent, String userPhone) {
        var station = stationServiceHelper.getByIdOrNew(rent.getReturnedToStationId());
        String msg = "\uD83D\uDC4C Returned PowerBank: " + rent.getPowerBankId() +
                "\n--\n<code>Station</code>: " + station.getShortId() +
                "\n<code>Place</code>: " + station.getPlaceName() +
                "\n<code>Price</code>: " + (rent.getPrice() / 100);
        messageToAdmin(msg);
    }

    public void rentStarted(Rent rent, String userPhone) {
        var station = stationServiceHelper.getByIdOrNew(rent.getTakenInStationId());
        String msg = "\uD83D\uDE01 Taken PowerBank: " + rent.getPowerBankId() +
                "\n--\n<code>Station</code>: " + station.getShortId() +
                "\n<code>Place</code>: " + station.getPlaceName() +
                "\n<code>User</code>: " + userPhone;
        messageToAdmin(msg);
    }

    public void rentError(Rent rent, String userPhone) {
        var station = stationServiceHelper.getByIdOrNew(rent.getTakenInStationId());
        String msg = "❌ Rent Error occurred: " + rent.getLastErrorMessage() +
                "\n--\n<code>Station:</code> " + station.getShortId() +
                "\n<code>Place</code>: " + station.getPlaceName() +
                "\n<code>User:</code> " + userPhone;
        messageToAdmin(msg);
    }

    public void errorFromMobileApp(String exception) {
// TODO: 28/09/2021 implement resending errors to Telegram and update mobile app
    }
}
