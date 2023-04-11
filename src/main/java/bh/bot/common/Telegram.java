package bh.bot.common;

import bh.bot.common.utils.InteractionUtil;
import bh.bot.common.utils.StringUtil;
import bh.bot.common.utils.InteractionUtil.Screen;

import java.awt.image.BufferedImage;
import java.io.BufferedOutputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.Buffer;

import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.generics.BotSession;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

import static bh.bot.common.Log.*;
import static bh.bot.common.utils.StringUtil.isBlank;
import static bh.bot.common.utils.ThreadUtil.sleep;

import static bh.bot.common.utils.ImageUtil.freeMem;

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

    private static BitHeroesTelegramBot bhTelegramBot = new BitHeroesTelegramBot();

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

    public static void sendPhoto(BufferedImage img, String caption, boolean critical) {
        if (isDisabled) {
            dev("disabled Telegram::sendPhoto");
            return;
        }
        boolean freeAfter = false;
        if (img == null) {
            freeAfter = true;
            int x = Configuration.gameScreenOffset.X.get();
            int y = Configuration.gameScreenOffset.Y.get();
            int w = Configuration.screenResolutionProfile.getSupportedGameResolutionWidth();
            int h = Configuration.screenResolutionProfile.getSupportedGameResolutionHeight();

            BufferedImage sc = InteractionUtil.Screen.captureScreen(x, y, w, h);
            img = sc;
        }

        int retry = critical ? 20 : 10;

        while (retry > 0) {
            try {
                if (internalSendPhoto(img, caption, critical))
                    break;
            } catch (Exception e) {
                e.printStackTrace();
                err("Error while posting Telegram message: %s", e.getMessage());
                sleep(30_000);
            } finally {
                retry--;
            }
        }
        if (freeAfter) {
            freeMem(img);
        }
    }

    private static boolean internalSendPhoto(BufferedImage image, String caption, boolean critical) throws Exception {
        caption = String.format("[%s%s]%s %s", appName, instanceId, critical ? " *** CRITICAL ***" : "", caption);

        bhTelegramBot.sendPhoto(image, caption);
        return true;
    }

    private static boolean internalSendMessage(String msg, boolean critical) throws Exception {
        msg = String.format("[%s%s]%s %s", appName, instanceId, critical ? " *** CRITICAL ***" : "", msg);

        bhTelegramBot.sendMessage(msg);
        return true;
    }
}
