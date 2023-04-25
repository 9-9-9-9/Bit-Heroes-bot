package bh.bot.common;

import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import static bh.bot.common.Log.*;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import javax.imageio.ImageIO;

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

    public void sendPhoto(BufferedImage img, String caption) {
        if (caption ==  null) {
            caption = "";
        }
        SendPhoto sendPic = new SendPhoto();
        sendPic.setChatId(Telegram.channelId);
        sendPic.setCaption(caption);
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        try {
            ImageIO.write(img, "jpeg", os);                          // Passing: ​(RenderedImage im, String formatName, OutputStream output)
            InputStream is = new ByteArrayInputStream(os.toByteArray());
            sendPic.setPhoto(new InputFile(is, caption));
            try {
                execute(sendPic);
            } catch (TelegramApiException ex) {
                ex.printStackTrace();
            }
        } catch (IOException ex) {
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