package bh.bot.common.jna;

import bh.bot.common.exceptions.InvalidDataException;
import bh.bot.common.types.Offset;
import bh.bot.common.types.ScreenResolutionProfile;
import bh.bot.common.types.tuples.Tuple2;
import bh.bot.common.types.tuples.Tuple4;
import bh.bot.common.utils.StringUtil;

import com.sun.jna.platform.DesktopWindow;
import com.sun.jna.platform.win32.WinDef;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;
import java.util.*;
import java.util.stream.Collectors;

import static bh.bot.common.Log.*;

public abstract class AbstractLinuxJna extends AbstractJna {
    @Override
    public DesktopWindow getGameWindow(Object... args) {
        return null;
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

    protected Tuple2<Boolean, List<String>> startProcess(String app, String... args) throws InterruptedException, IOException {
        debug("App: %s, commands: %s", app, String.join(", ", args));
        File fileTmpOutput = new File(String.format("/tmp/bh-tmp.%s.out", UUID.randomUUID().toString()));
        try {
            if (!fileTmpOutput.createNewFile())
                return new Tuple2<>(false, null);
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
                //noinspection ResultOfMethodCallIgnored
                fileTmpOutput.delete();
            } catch (Exception ex2) {
                ex2.printStackTrace();
            }
        }
    }

    private List<String> readOutputLines(File file) throws IOException {
        List<String> output = Files.readAllLines(file.toPath()).stream().filter(StringUtil::isNotBlank).collect(Collectors.toList());
        if (output.size() == 0)
            throw new InvalidDataException("File %s contains no data", file.getName());
        return output;
    }
}
