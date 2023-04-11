package bh.bot.common;

import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import static bh.bot.common.Log.*;

public class BitHeroesTelegramBot extends TelegramLongPollingBot {
    @Override
    public void onUpdateReceived(Update update) {
    }

    public void sendMessage(String msg) {
        SendMessage sendMsg = new SendMessage();
        sendMsg.setChatId(Telegram.channelId);
        sendMsg.setText(msg);
        try {
            execute(sendMsg);
        } catch (TelegramApiException ex) {
            ex.printStackTrace();
        }
    }

    @Override
    public String getBotUsername() {
        // Return bot username
        // If bot username is @MyAmazingBot, it must return 'MyAmazingBot'
        return "Bit Heroes Bot";
    }

    @Override
    public String getBotToken() {
        // Return bot token from BotFather
        return Telegram.token;
    }
}