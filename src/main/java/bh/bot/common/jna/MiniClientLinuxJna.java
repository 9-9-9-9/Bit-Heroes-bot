package bh.bot.common.jna;

import bh.bot.common.exceptions.NotImplementedException;
import bh.bot.common.types.tuples.Tuple2;
import com.sun.jna.platform.win32.WinDef.HWND;

import java.awt.*;
import java.util.List;
import java.util.stream.Collectors;

import static bh.bot.common.Log.debug;

public class MiniClientLinuxJna extends AbstractLinuxJna {
    @Override
    public Rectangle getRectangle(HWND hwnd) {
        throw new NotImplementedException("MiniClientLinuxJna::getRectangle");
    }

    @Override
    public void internalTryToCloseGameWindow() throws Exception {
        Tuple2<Boolean, List<String>> prcResult = startProcess("ps", "-ao", "pid:1,command:1");
        if (!prcResult._1) {
            debug("internalTryToCloseGameWindow: Failure at grep mini-client info from chrome processes");
            return;
        }
        List<String> psOutput = prcResult._2.stream()
                .filter(x -> x.contains("chrome") && x.contains("bh-client/index"))
                .collect(Collectors.toList());
        if (psOutput.size() < 1) {
        	debug("internalTryToCloseGameWindow: Unacceptable result from ps command");
        	return;
		}
        for (String s : psOutput) {
            String[] spl = s.split(" ", 2);
            if (spl.length != 2) {
                debug("internalTryToCloseGameWindow: Output of ps command has invalid format: %s", psOutput);
                continue;
            }
            int pid = Integer.parseInt(spl[0]);
            startProcess("kill", "-9", String.valueOf(pid));

            new ProcessBuilder(new String[]{
                    "kill", "-9", String.valueOf(pid)
            }).start().waitFor();
        }
    }
}