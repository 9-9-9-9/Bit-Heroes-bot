package bh.bot.app.dev;

import bh.bot.app.AbstractApplication;
import bh.bot.common.Configuration;
import bh.bot.common.exceptions.InvalidDataException;
import bh.bot.common.types.ScreenResolutionProfile;
import bh.bot.common.types.annotations.AppMeta;
import bh.bot.common.types.tuples.Tuple3;
import bh.bot.common.utils.ImageUtil;
import bh.bot.common.utils.StringUtil;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static bh.bot.Main.readInput;
import static bh.bot.common.Log.info;
import static bh.bot.common.Log.warn;

@AppMeta(code = "tp", name = "Import Tolerant-Pixel", requireClientType = false, dev = true, displayOrder = 100)
public class ImportTpImageApp extends AbstractApplication {
    @Override
    protected void internalRun(String[] args) {
    	
        Configuration.Tolerant.colorBw = 60;
        warn("Forced value of Configuration.Tolerant.colorBw to %d", Configuration.Tolerant.colorBw);

        int rgb = Integer.parseInt(args[0], 16) & 0xFFFFFF;
        int tolerant = Configuration.Tolerant.colorBw;

        info("Keep RGB: %s", args[0]);
        info("Tolerant: %d", tolerant);
        info("Input file: %s", args[1]);

        try {
            BufferedImage bi = loadImageFromFile(args[1]);
            
            String profileName;
            String filePath;
            final List<Opt> opts = Arrays.asList(new Opt("Button", "buttons"), new Opt("Dialog", "dialogs"),
                    new Opt("Label", "labels"));

            /*
            profileName = readInput("Which profile?\n  1. Web\n  2. Steam", null, s -> {
                s = s.trim();
                if (s.equals("1"))
                    return new Tuple3<>(true, null, "web");
                if (s.equals("2"))
                    return new Tuple3<>(true, null, "steam");
                return new Tuple3<>(false, "Not supported", null);
            });
             */

            profileName = ScreenResolutionProfile.Profile800x520.profileName;

            String group = readInput("Group?", "eg: globally", s -> {
                s = s.trim();
                if (StringUtil.isBlank(s))
                    return new Tuple3<>(false, "Must not blank", null);
                return new Tuple3<>(true, null, s);
            });

            String name = readInput("name?", "eg: town", s -> {
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

            final String fileName = String.format("%s.%s-tp.bmp", group, name);

            StringBuilder sb = new StringBuilder("Which kind?\n");
            for (int i = 0; i < opts.size(); i++)
                sb.append(String.format("%d. %s\n", i + 1, opts.get(i).name));

            filePath = readInput(sb.toString(), null, s -> {
                s = s.trim();
                int selected = Integer.parseInt(s);
                Opt opt = opts.get(selected - 1);
                String[] mergedPath = Stream
                        .concat(Stream.of("main", "resources", "game-images", profileName),
                                Arrays.stream(opt.path)).toArray(String[]::new);
                Path targetDir = Paths.get("src", mergedPath);
                File dir = targetDir.toFile();
                if (!dir.exists())
                    return new Tuple3<>(false, String.format("Directory is not exists: %s", dir.getAbsolutePath()),
                            null);
                File targetFile = Paths.get(dir.getAbsolutePath(), fileName).toFile();
                if (targetFile.exists())
                    return new Tuple3<>(false,
                            String.format("Target file is already exists: %s", targetFile.getAbsolutePath()), null);
                return new Tuple3<>(true, null, targetFile.getAbsolutePath());
            });

            ImageUtil.TestTransformMxResult testTransformMxResult = ImageUtil.testTransformMx(bi, rgb, tolerant);
            
            saveImage(testTransformMxResult.mx, "mx");

            File file = new File(filePath);
            if (file.exists())
                throw new InvalidDataException("File exists!!!");
            ImageIO.write(testTransformMxResult.tp, "bmp", file);

            info("Saved TP image with name '%s' to %s", file.getName(), file.getAbsolutePath());
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
    protected String getUsage() {
        return "<KeepHexColor> <image>";
    }

    @Override
    protected String getDescription() {
        return "(developers only) Import tolerant pixel image";
    }

    @Override
    protected String getLimitationExplain() {
        return null;
    }

    @Override
    protected boolean isRequiredToLoadImages() {
        return false;
    }
}
