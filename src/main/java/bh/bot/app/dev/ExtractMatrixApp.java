package bh.bot.app.dev;

import bh.bot.Main;
import bh.bot.app.AbstractApplication;
import bh.bot.common.types.annotations.AppMeta;
import bh.bot.common.types.tuples.Tuple3;
import bh.bot.common.utils.ImageUtil;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.function.Function;

import static bh.bot.Main.readInput;
import static bh.bot.common.Log.info;
import static bh.bot.common.Log.warn;

@SuppressWarnings("DeprecatedIsStillUsed")
@AppMeta(code = "matrix", name = "Matrix", dev = true)
@Deprecated
/*
  Please use class ImportTpImageApp (app code: `tp`)
 */
public class ExtractMatrixApp extends AbstractApplication {
    @Override
    protected void internalRun(String[] args) {
        try {
            if (args.length != 0 && args.length != 3) {
                info("Invalid number of arguments");
                info(getHelp());
                System.exit(Main.EXIT_CODE_INVALID_NUMBER_OF_ARGUMENTS);
                return;
            }

            boolean isDisplayedHelp = false;
            int rgb, tolerant;
            String img;
            try {
                rgb = Integer.parseInt(args[0], 16) & 0xFFFFFF;
            } catch (ArrayIndexOutOfBoundsException | NumberFormatException ex) {
                info(getHelp());
                isDisplayedHelp = true;
                rgb = readInput("Color to keep:", "6 characters, in hex format, without prefix '0x', for example: FFFFFF is white, or FFC0CB is pink", s -> {
                    final String normalized = s.trim().toUpperCase();
                    if (normalized.length() != 6)
                        return new Tuple3<>(false, "Must be 6 characters in length", 0);
                    for (char c : normalized.toCharArray())
                        if (c < 48 || (c > 57 && c < 65) || c > 70)
                            return new Tuple3<>(false, "Contains invalid Hexadecimal character, must be 0-9 A-F", 0);
                    try {
                        int result = Integer.parseInt(normalized, 16) & 0xFFFFFF;
                        return new Tuple3<>(true, null, result);
                    } catch (Exception ex2) {
                        return new Tuple3<>(false, "Unable to parse, error: " + ex.getMessage(), 0);
                    }
                });
            }

            try {
                tolerant = Math.abs(Integer.parseInt(args[1]));
            } catch (ArrayIndexOutOfBoundsException | NumberFormatException ex) {
                info(getHelp());
                isDisplayedHelp = true;
                tolerant = readInput("Tolerant:", "Tolerant when color contains same R & G & B value", s -> {
                    try {
                        int result = Integer.parseInt(s.trim());
                        if (result < 0)
                            return new Tuple3<>(false, "Must be equals or greater than 0", 0);
                        if (result > 100)
                            return new Tuple3<>(false, "Must be equals or lower than 100", 0);
                        return new Tuple3<>(true, null, result);
                    } catch (Exception ex2) {
                        return new Tuple3<>(false, "Unable to parse, error: " + ex.getMessage(), 0);
                    }
                });
            }

            try {
                img = args[2];
            } catch (ArrayIndexOutOfBoundsException ex) {
                if (!isDisplayedHelp)
                    info(getHelp());
                Function<String, Tuple3<Boolean, String, String>> transform = s -> {
                    try {
                        File file = new File(s);
                        if (!file.exists())
                            return new Tuple3<>(false, "File does not exists", null);
                        return new Tuple3<>(true, null, s);
                    } catch (Exception ex2) {
                        return new Tuple3<>(false, "Invalid file path", null);
                    }
                };
                img = readInput("File path of the picture you want to extract:", null, transform);
            }

            info("Keep RGB %d", rgb);

            BufferedImage bi = loadImageFromFile(img);

            ImageUtil.TestTransformMxResult testTransformMxResult = ImageUtil.testTransformMx(bi, rgb, tolerant);

            saveImage(testTransformMxResult.mx, "result");
            saveImage(testTransformMxResult.tp, "a-tp");

            info("Done %dx%d from offset %d,%d", testTransformMxResult.mx.getWidth(), testTransformMxResult.mx.getHeight(), testTransformMxResult.mxOffset.X, testTransformMxResult.mxOffset.Y);

            warn("This function is deprecated, please use `tp` instead");
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    @Override
    protected String getUsage() {
        return "<KeepHexColor> <tolerant> <image>";
    }

    @Override
    protected String getDescription() {
        return "(developers only) Read image, keep only pixels which has color (eg. FF0000 is red) as the same as input. Used to produce picture for BwMatrixMeta objects";
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
