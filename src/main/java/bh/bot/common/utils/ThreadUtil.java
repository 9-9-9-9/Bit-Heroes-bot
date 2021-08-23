package bh.bot.common.utils;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import static bh.bot.common.Log.err;
import static bh.bot.common.Log.info;

public class ThreadUtil {
    public static void waitDone(Runnable... runAbles) {
        List<CompletableFuture<Void>> completableFutures = Arrays.stream(runAbles).map(CompletableFuture::runAsync).collect(Collectors.toList());
        while (!completableFutures.stream().allMatch(CompletableFuture::isDone)) {
            sleep(3000);
        }
        info("waitDone finished");
    }

    public static void sleep(int ms) {
        try {
            Thread.sleep(Math.max(10, ms));
        } catch (InterruptedException ex) {
            err("Failure to sleep");
        }
    }
}
