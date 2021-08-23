package bh.bot;

import bh.bot.common.Configuration;
import bh.bot.app.AbstractApplication;
import bh.bot.app.TestApp;
import bh.bot.app.GenMiniClient;
import bh.bot.app.KeepPixApp;
import bh.bot.app.ExtractMatrixApp;
import bh.bot.app.SamePixApp;
import bh.bot.app.FishingApp;
import bh.bot.app.ReRunApp;
import bh.bot.common.utils.InteractionUtil;

public class Main {
    public static void main(String[] args) throws Exception {
        Configuration.load();
        Configuration.registerApplicationInstances(
                new ReRunApp(),
                new FishingApp(),
                new GenMiniClient(),
                //
                new KeepPixApp(),
                new ExtractMatrixApp(),
                new SamePixApp(),
                //
                new TestApp()
        );
        InteractionUtil.init();
        AbstractApplication.LaunchInfo launchInfo = AbstractApplication.parse(args);
        if (launchInfo.displayHelp) {
            System.out.println(launchInfo.instance.getHelp());
            return;
        }
        launchInfo.instance.run(launchInfo);
    }
}
