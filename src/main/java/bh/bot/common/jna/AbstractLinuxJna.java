package bh.bot.common.jna;

import bh.bot.common.Configuration;
import bh.bot.common.exceptions.InvalidDataException;
import bh.bot.common.types.ScreenResolutionProfile;
import bh.bot.common.types.tuples.Tuple2;
import bh.bot.common.types.tuples.Tuple4;
import bh.bot.common.utils.StringUtil;
import com.sun.jna.platform.win32.WinDef;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;

import static bh.bot.common.Log.*;

public abstract class AbstractLinuxJna extends AbstractJna {
    @Override
    public WinDef.HWND getGameWindow(Object... args) {
        return null;
    }

    @Override
    public Tuple4<Boolean, String, Rectangle, Configuration.Offset> locateGameScreenOffset(
            WinDef.HWND hwnd, ScreenResolutionProfile screenResolutionProfile) {
        if (hwnd != null)
            throw new IllegalArgumentException("hwnd");
        try {
            Tuple2<Boolean, List<String>> prcResult = startProcess("ps", "-ao", "pid:1,command:1");
            if (!prcResult._1)
                return new Tuple4<>(false, "Failure at grep mini-client info from chrome processes", null, null);
            List<String> psOutput = prcResult._2.stream()
                    .filter(x -> x.contains("chrome") && x.contains("bh-client/index"))
                    .collect(Collectors.toList());
            if (psOutput.size() != 1)
                return new Tuple4<>(false, "Unacceptable result from ps command", null, null);
            String[] spl = psOutput.get(0).split(" ", 2);
            if (spl.length != 2)
                throw new InvalidDataException("Output of ps command has invalid format: %s", psOutput);
            int pid;
            try {
                pid = Integer.parseInt(spl[0]);
            } catch (NumberFormatException ex3) {
                throw new InvalidDataException("Failure on parsing PID from ps command: %s", psOutput);
            }
            prcResult = startProcess("xdotool", "search",  "--pid", String.valueOf(pid), "getwindowgeometry");
            if (!prcResult._1)
                return new Tuple4<>(false, "Failure at grep window id of chrome processes from xdotool", null, null);
            if (prcResult._2.size() < 1) {
                return new Tuple4<>(false, "Unacceptable result from xdotool command", null, null);
            }

            for (String xdoOutput : prcResult._2) {
                int windowId;
                try {
                    windowId = Integer.parseInt(xdoOutput);
                } catch (NumberFormatException ex3) {
                    err("Failure on parsing window id from output from xdotool command: %s", xdoOutput);
                    continue;
                }

                prcResult = startProcess("xwininfo", "-id", String.valueOf(windowId));
                if (!prcResult._1) {
                    err("Failure at grep window info from xwininfo for %d", windowId);
                    continue;
                }

                List<String> xwiOutput = prcResult._2;
                if (!xwiOutput.stream().anyMatch(x -> x.contains("Bit Heroes"))) {
                    debug("%d is not mini-client", windowId);
                    continue;
                }
                Optional<String> xL = xwiOutput.stream().filter(x -> x.contains("Absolute upper-left X:")).findFirst();
                Optional<String> yL = xwiOutput.stream().filter(x -> x.contains("Absolute upper-left Y:")).findFirst();

                if (!xL.isPresent() || !yL.isPresent())
                    throw new InvalidDataException("Unable to locate X and Y from output of xwininfo (%s)", String.join(", ", xwiOutput));

                int x, y;
                try {
                    x = parseXOrYValue(xL.get().trim());
                    y = parseXOrYValue(yL.get().trim());
                } catch (IllegalArgumentException ex4) {
                    return new Tuple4<>(false, ex4.getMessage(), null, null);
                }

                return new Tuple4<>(true, null, null, new Configuration.Offset(x, y));
            }

            return new Tuple4<>(false, "Failure at grep window info from xwininfo", null, null);
        } catch (Exception ex) {
            ex.printStackTrace();
            return new Tuple4<>(false, "Error occurs: " + ex.getMessage(), null, null);
        }
    }

    private int parseXOrYValue(String raw) {
        String[] split = raw.split(":", 2);
        if (split.length == 2)
            try {
                return Integer.parseInt(split[1].trim());
            } catch (NumberFormatException ex) {
                //
            }
        throw new IllegalArgumentException(String.format("Input data has incorrect format: %s", raw));
    }

    private Tuple2<Boolean, List<String>> startProcess(String app, String... args) throws InterruptedException, IOException {
        debug("App: %s, commands: %s", app, String.join(", ", args));
        File fileTmpOutput = new File(String.format("/tmp/bh-tmp.%s.out", UUID.randomUUID().toString()));
        try{
            fileTmpOutput.createNewFile();
            ArrayList<String> nArgs = new ArrayList<>();
            nArgs.add(app);
            nArgs.addAll(Arrays.asList(args));
            ProcessBuilder pb = new ProcessBuilder(nArgs);
            pb.redirectOutput(fileTmpOutput);
            Process p = pb.start();
            int exitCode = p.waitFor();
            optionalDebug(exitCode != 0, "Exit code: %d", exitCode);
            if (exitCode != 0)
                return new Tuple2<>(false, null);
            return new Tuple2<>(true, readOutputLines(fileTmpOutput));
        } finally {
            try {
                fileTmpOutput.delete();
            } catch (Exception ex2) {
                ex2.printStackTrace();
            }
        }
    }

    private String readOutput(File file) throws IOException {
        List<String> output = readOutputLines(file);
        if (output.size() > 1)
            throw new InvalidDataException("More than one line on %s", file.getName());
        return output.get(0);
    }

    private List<String> readOutputLines(File file) throws IOException {
        List<String> output = Files.readAllLines(file.toPath()).stream().filter(x -> StringUtil.isNotBlank(x)).collect(Collectors.toList());
        if (output.size() == 0)
            throw new InvalidDataException("File %s contains no data", file.getName());
        return output;
    }
}
