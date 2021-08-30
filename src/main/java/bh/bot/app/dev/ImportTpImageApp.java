package bh.bot.app.dev;

import bh.bot.app.AbstractApplication;
import bh.bot.common.Configuration;
import bh.bot.common.exceptions.InvalidDataException;
import bh.bot.common.types.annotations.AppCode;
import bh.bot.common.types.tuples.Tuple3;
import bh.bot.common.utils.ImageUtil;
import bh.bot.common.utils.StringUtil;

import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static bh.bot.common.Log.info;

@AppCode(code = "tp")
public class ImportTpImageApp extends AbstractApplication {
    @Override
    protected void internalRun(String[] args) {
        int rgb = Integer.parseInt(args[0], 16) & 0xFFFFFF;
        int tolerant = Configuration.Tolerant.colorBw;

        info("Keep RGB: %s", args[0]);
        info("Tolerant: %d", tolerant);
        info("Input file: %s", args[1]);

        try {
            String profileName;
            String filePath;
            final List<Opt> opts = Arrays.asList(
                    new Opt("Button", "buttons"),
                    new Opt("Dialog", "dialogs"),
                    new Opt("Label", "labels")
            );
            try (
                    InputStreamReader isr = new InputStreamReader(System.in);
                    BufferedReader br = new BufferedReader(isr);
            ) {
                info("1. Web");
                info("2. Steam");
                profileName = readInput(br, "Which profile?", null, s -> {
                    s = s.trim();
                    if (s == "1")
                        return new Tuple3<>(true, null, "web");
                    if (s == "2")
                        return new Tuple3<>(true, null, "steam");
                    return new Tuple3<>(false, "Not supported", null);
                });

                String group = readInput(br, "Group?", "eg: globally", s -> {
                    s = s.trim();
                    if (StringUtil.isBlank(s))
                        return new Tuple3<>(false, "Must not blank", null);
                    return new Tuple3<>(true, null, s);
                });

                String name = readInput(br, "name?", "eg: town", s -> {
                    s = s.trim();
                    if (StringUtil.isBlank(s))
                        return new Tuple3<>(false, "Must not blank", null);
                    if (s.endsWith("bmp"))
                        return new Tuple3<>(false, "Can not ends with 'bmp'", null);
                    if (s.endsWith("-mx"))
                        return new Tuple3<>(false, "Can not ends with '-mx'", null);
                    if (s.endsWith("-tp"))
                        return new Tuple3<>(false, "Can not ends with '-tp'", null);
                    return new Tuple3<>(true, null, s);
                });

                final String fileName = String.format("%s.%s-tp.bmp");

                for (int i = 0; i < opts.size(); i++)
                    info("%d. %s", i + 1, opts.get(i).name);

                filePath = readInput(br, "Which kind?", null, s -> {
                    s = s.trim();
                    int selected = Integer.parseInt(s);
                    final String pn = profileName;
                    Opt opt = opts.get(selected - 1);
                    String[] mergedPath = Stream.concat(
                            Arrays.asList("main", "resources", "game-images", pn).stream(),
                            Arrays.asList(opt.path).stream()
                    ).collect(Collectors.toList()).toArray(new String[0]);
                    Path targetDir = Paths.get("src", mergedPath);
                    File dir = targetDir.toFile();
                    if (!dir.exists())
                        return new Tuple3<>(false, String.format("Directory is not exists: %s", dir.getAbsolutePath()), null);
                    File targetFile = Paths.get(dir.getAbsolutePath(), fileName).toFile();
                    if (targetFile.exists())
                        return new Tuple3<>(false, String.format("Target file is already exists: %s", targetFile.getAbsolutePath()), null);
                    return new Tuple3<>(true, null, targetFile.getAbsolutePath());
                });
            } catch (IOException e) {
                throw e;
            }

            BufferedImage bi = loadImageFromFile(args[1]);

            ImageUtil.TestTransformMxResult testTransformMxResult = ImageUtil.testTransformMx(bi, rgb, tolerant);

            File file = new File(filePath);
            if (file.exists())
                throw new InvalidDataException("File exists!!!");
            saveImage(testTransformMxResult.tp, "bmp, file");

            info("Done");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static class Opt {
        public final String name;
        public final String[] path;

        private Opt(String name, String... path) {
            this.name = name;
            this.path = path;
        }
    }

    @Override
    protected String getAppName() {
        return "BH-Import TP Image";
    }

    @Override
    protected String getScriptFileName() {
        return "tp";
    }

    @Override
    protected String getUsage() {
        return "<KeepHexColor> <image>";
    }

    @Override
    protected String getDescription() {
        return "(developers only) Import tp image";
    }

    @Override
    protected String getLimitationExplain() {
        return null;
    }
}
