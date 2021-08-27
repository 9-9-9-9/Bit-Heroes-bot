package bh.bot.common;

import bh.bot.common.utils.ThreadUtil;

import java.io.BufferedOutputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;

import static bh.bot.common.utils.StringUtil.isBlank;
import static bh.bot.common.utils.StringUtil.isNotBlank;

public class Telegram {
    private static String appName = "BH-Unknown";

    //
    private static final String token = Configuration.getFromConfigOrEnv("telegram.token", "TELEGRAM_BOT_KEY");
    //
    private static final String channelId = Configuration.getFromConfigOrEnv("telegram.channel-id", "TELEGRAM_BH_CHANNEL");
    //
    private static boolean isDisabled = !isNotBlank(token) && !isNotBlank(channelId);

    public static boolean isDisabled() {
        return isDisabled;
    }

    public static void disable() {
        isDisabled = true;
        Log.info("Disabled Telegram messages");
    }

    public static void setAppName(String appName) {
        if (isBlank(appName))
            return;

        Telegram.appName = appName;
    }

    public static void sendMessage(String msg, boolean critical) {
        if (isDisabled) {
            return;
        }

        int retry = 10;

        while (retry > 0) {
            try {
                if (internalSendMessage(msg, critical))
                    break;
            } catch (Exception e) {
                e.printStackTrace();
                Log.err("Error while posting Telegram message: %s", e.getMessage());
                ThreadUtil.sleep(30000);
            } finally {
                retry--;
            }
        }
    }

    private static boolean internalSendMessage(String msg, boolean critical) throws Exception {
        msg = String.format("[%s]%s %s", appName, critical ? " *** CRITICAL ***" : "", msg);

        String my_url = "https://api.telegram.org/bot" + token + "/sendMessage";
        URL url = new URL(my_url);
        HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
        httpURLConnection.setRequestMethod("POST");
        httpURLConnection.setRequestProperty("Content-Type", "application/json");
        try {
            httpURLConnection.setDoOutput(true);
            httpURLConnection.setChunkedStreamingMode(0);
            OutputStream outputStream = new BufferedOutputStream(httpURLConnection.getOutputStream());
            OutputStreamWriter outputStreamWriter = new OutputStreamWriter(outputStream);
            outputStreamWriter.write("{\"chat_id\":\"" + channelId + "\",\"text\":\"" + msg + "\"}");
            outputStreamWriter.flush();
            outputStreamWriter.close();

            Log.debug("Telegram.sendMessage: sent");
            int responseCode = httpURLConnection.getResponseCode();
            Log.debug("RC: %d", responseCode);
            Log.debug("RM: %s", httpURLConnection.getResponseMessage());
            return responseCode == 200;
        } finally {
            httpURLConnection.disconnect();
        }
    }
}
