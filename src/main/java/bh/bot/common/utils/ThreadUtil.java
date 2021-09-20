package bh.bot.common.utils;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

import static bh.bot.common.Log.*;

public class ThreadUtil {
	public static void waitDone(Runnable... runAbles) {
		ExecutorService pool = Executors.newFixedThreadPool(runAbles.length);
		List<CompletableFuture<Void>> completableFutures = Arrays.stream(runAbles)
				.map(x -> CompletableFuture.runAsync(x, pool)).collect(Collectors.toList());
		while (!completableFutures.stream().allMatch(CompletableFuture::isDone)) {
			sleep(3000);
		}
		
		String pattern = "HH:mm";
		DateFormat df = new SimpleDateFormat(pattern);
		Date today = Calendar.getInstance().getTime();
		String now = df.format(today);

		info("waitDone finished at %s", now);
		debug("ExecutorService is shutting down now");
		pool.shutdownNow();
	}

	public static void sleep(int ms) {
		try {
			Thread.sleep(Math.max(10, ms));
		} catch (InterruptedException ex) {
			err("Failure to sleep");
		}
	}
}
