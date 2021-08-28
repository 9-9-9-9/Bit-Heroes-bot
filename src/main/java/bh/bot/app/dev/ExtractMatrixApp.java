package bh.bot.app.dev;

import bh.bot.Main;
import bh.bot.app.AbstractApplication;
import bh.bot.common.Configuration;
import bh.bot.common.types.annotations.AppCode;
import bh.bot.common.types.tuples.Tuple3;
import bh.bot.common.utils.ImageUtil;

import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

import static bh.bot.common.Log.info;

@AppCode(code = "matrix")
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
            try (
                    InputStreamReader isr = new InputStreamReader(System.in);
                    BufferedReader br = new BufferedReader(isr);
            ) {
                try {
                    rgb = Integer.parseInt(args[0], 16) & 0xFFFFFF;
                } catch (ArrayIndexOutOfBoundsException | NumberFormatException ex) {
                    info(getHelp());
                    isDisplayedHelp = true;
                    rgb = readInput(br, "Color to keep:", "6 characters, in hex format, without prefix '0x', for example: FFFFFF is white, or FFC0CB is pink", new Function<String, Tuple3<Boolean, String, Integer>>() {
                        @Override
                        public Tuple3<Boolean, String, Integer> apply(String s) {
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
                        }
                    });
                }

                try {
                    tolerant = Math.abs(Integer.parseInt(args[1]));
                } catch (ArrayIndexOutOfBoundsException | NumberFormatException ex) {
                    info(getHelp());
                    isDisplayedHelp = true;
                    tolerant = readInput(br, "Tolerant", "Tolerant when color contains same R & G & B value", new Function<String, Tuple3<Boolean, String, Integer>>() {
                        @Override
                        public Tuple3<Boolean, String, Integer> apply(String s) {
                            String normalized = s.trim().toUpperCase();
                            if (normalized.length() != 6)
                                return new Tuple3<>(false, "Must be 6 characters in length", 0);
                            for (char c : normalized.toCharArray())
                                if (c < 48 || (c > 57 && c < 65) || c > 70)
                                    return new Tuple3<>(false, "Contains invalid Hexadecimal character, must be 0-9 A-F", 0);
                            try {
                                int result = Integer.parseInt(normalized);
                                if (result < 0)
                                    return new Tuple3<>(false, "Must be equals or greater than 0", 0);
                                return new Tuple3<>(true, null, result);
                            } catch (Exception ex2) {
                                return new Tuple3<>(false, "Unable to parse, error: " + ex.getMessage(), 0);
                            }
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
                    img = readInput(br, "File path of the picture you want to extract:", null, transform);
                }
            } catch (IOException ex) {
                ex.printStackTrace();
                System.exit(Main.EXIT_CODE_UNHANDLED_EXCEPTION);
                throw ex;
            }

            info("Keep RGB %d", rgb);

            BufferedImage bi = loadImageFromFile(img);

            int minX = Integer.MAX_VALUE;
            int minY = Integer.MAX_VALUE;
            int maxX = Integer.MIN_VALUE;
            int maxY = Integer.MIN_VALUE;

            final ImageUtil.DynamicRgb dRgb = new ImageUtil.DynamicRgb(rgb, tolerant);

            List<int[]> pos = new ArrayList<>();
            for (int y = 0; y < bi.getHeight(); y++) {
                for (int x = 0; x < bi.getWidth(); x++) {
                    int posRgb = bi.getRGB(x, y) & 0xFFFFFF;
                    if (!ImageUtil.areColorsSimilar(dRgb, posRgb, Configuration.Tolerant.color)) {
                        continue;
                    }
                    pos.add(new int[]{x, y});

                    minX = Math.min(minX, x);
                    minY = Math.min(minY, y);
                    maxX = Math.max(maxX, x);
                    maxY = Math.max(maxY, y);
                }
            }

            List<int[]> translatedPos = new ArrayList<>();
            for (int[] p : pos) {
                translatedPos.add(new int[]{p[0] - minX, p[1] - minY});
            }

            final int white = 0xFFFFFF;
            final int black = 0x000000;
            final int red = 0xFF0000;

            BufferedImage biSample = new BufferedImage(maxX - minX + 1, maxY - minY + 1, 5);
            for (int[] p : translatedPos)
                biSample.setRGB(p[0], p[1], white);

            BufferedImage biTransformed = new BufferedImage(biSample.getWidth(), biSample.getHeight(), biSample.getType());
            for (int y = 0; y < biTransformed.getHeight(); y++) {
                for (int x = 0; x < biTransformed.getWidth(); x++) {
                    biTransformed.setRGB(x, y, white);
                }
            }

            for (int y = 0; y < biSample.getHeight(); y++) {
                for (int x = 0; x < biSample.getWidth(); x++) {
                    int rgbFromSample = biSample.getRGB(x, y) & 0xFFFFFF;
                    if (rgbFromSample != white) {
                        // biTransformed.setRGB(x, y, white);
                        continue;
                    }
                    // rgbFromSample is white
                    biTransformed.setRGB(x, y, black);
                    for (int oY = -1; oY <= 1; oY++) {
                        for (int oX = -1; oX <= 1; oX++) {
                            if (oX == 0 && oY == 0)
                                continue;
                            int sX = x + oX;
                            int sY = y + oY;
                            if (sX < 0 || sX >= biSample.getWidth())
                                continue;
                            if (sY < 0 || sY >= biSample.getHeight())
                                continue;
                            rgbFromSample = biSample.getRGB(sX, sY) & 0xFFFFFF;
                            if (rgbFromSample == black)
                                biTransformed.setRGB(sX, sY, red);
                        }
                    }
                }
            }

            saveImage(biSample, "cropped-output-sample");
            saveImage(biTransformed, "result");

            info("Done %dx%d", biSample.getWidth(), biSample.getHeight());
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    @Override
    protected String getAppName() {
        return "BH-Extract Matrix";
    }

    @Override
    protected String getScriptFileName() {
        return "matrix";
    }

    @Override
    protected String getUsage() {
        return "<KeepHexColor> <image>";
    }

    @Override
    protected String getDescription() {
        return "(developers only) Read image, keep only pixels which has color (eg. FF0000 is red) as the same as input. Used to produce picture for BwMatrixMeta objects";
    }

    @Override
    protected String getLimitationExplain() {
        return null;
    }
}
