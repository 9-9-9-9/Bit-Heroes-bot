package bh.bot.common;

import bh.bot.common.utils.StringUtil;

import java.io.BufferedOutputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;

import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.generics.BotSession;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

import static bh.bot.common.Log.*;
import static bh.bot.common.utils.StringUtil.isBlank;
import static bh.bot.common.utils.ThreadUtil.sleep;

public class Telegram {
    private static String appName = "BH-Unknown";

    //
    public static final String token = Configuration.getFromConfigOrEnv("telegram.token", "TELEGRAM_BOT_KEY");
    //
    public static final String channelId = Configuration.getFromConfigOrEnv("telegram.channel-id", "TELEGRAM_BH_CHANNEL");
    //
    private static final String _instanceId = Configuration.getFromConfigOrEnv("telegram.instance-id", "TELEGRAM_INSTANCE_ID");
    public static final String instanceId = StringUtil.isBlank(_instanceId) ? "" : String.format(":%s", _instanceId.trim());
    //
    private static boolean isDisabled = isBlank(token) || isBlank(channelId);

    public static boolean isDisabled() {
        return isDisabled;
    }

    public static TelegramBotsApi botApi;

    public static BitHeroesTelegramBot bhTelegramBot = new BitHeroesTelegramBot();

    public static BotSession botSession;

    public static void init() {
        try {
            
            botApi = new TelegramBotsApi(DefaultBotSession.class);
            
            dev("init Telegram API");
            botSession = botApi.registerBot(bhTelegramBot);
            
        } catch (TelegramApiException ex) {
            ex.printStackTrace();
        }

    }

    public static void disable() {
        isDisabled = true;
        info("Disabled Telegram messages");
    }

    public static void setAppName(String appName) {
        if (isBlank(appName))
            return;

        Telegram.appName = appName;
    }

    public static void sendMessage(String msg, boolean critical) {
        if (isDisabled) {
            dev("disabled Telegram::sendMessage");
            return;
        }

        int retry = critical ? 20 : 10;

        while (retry > 0) {
            try {
                if (internalSendMessage(msg, critical))
                    break;
            } catch (Exception e) {
                e.printStackTrace();
                err("Error while posting Telegram message: %s", e.getMessage());
                sleep(30_000);
            } finally {
                retry--;
            }
        }
    }

    private static boolean internalSendMessage(String msg, boolean critical) throws Exception {
        msg = String.format("[%s%s]%s %s", appName, instanceId, critical ? " *** CRITICAL ***" : "", msg);

        bhTelegramBot.sendMessage(msg);
        return true;

        // String my_url = "https://api.telegram.org/bot" + token + "/sendMessage";
        // URL url = new URL(my_url);
        // HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
        // httpURLConnection.setRequestMethod("POST");
        // httpURLConnection.setRequestProperty("Content-Type", "application/json");
        // try {
        //     httpURLConnection.setDoOutput(true);
        //     httpURLConnection.setChunkedStreamingMode(0);
        //     OutputStream outputStream = new BufferedOutputStream(httpURLConnection.getOutputStream());
        //     OutputStreamWriter outputStreamWriter = new OutputStreamWriter(outputStream);
        //     outputStreamWriter.write("{\"chat_id\":\"" + channelId + "\",\"text\":\"" + msg + "\"}");
        //     outputStreamWriter.flush();
        //     outputStreamWriter.close();

        //     int responseCode = httpURLConnection.getResponseCode();

        //     if (responseCode == 200)
        //         return true;

        //     err("Telegram::internalSendMessage failure with response code: %d, response msg: %s", responseCode, httpURLConnection.getResponseMessage());
        //     return false;
        // } finally {
        //     httpURLConnection.disconnect();
        // }
    }
}
