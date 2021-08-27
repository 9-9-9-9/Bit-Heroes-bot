package bh.bot.app.dev;

import bh.bot.Main;
import bh.bot.app.AbstractApplication;
import bh.bot.common.types.annotations.AppCode;
import bh.bot.common.types.tuples.Tuple3;

import java.awt.image.BufferedImage;
import java.io.*;
import java.util.function.Function;

import static bh.bot.common.Log.err;
import static bh.bot.common.Log.info;

@AppCode(code = "samepix")
@Deprecated
public class SamePixApp extends AbstractApplication {
    @Override
    protected void internalRun(String[] args) {
        try {
            throwNotSupportedFlagExit(argumentInfo.exitAfterXSecs);
            String img1, img2;

            try (
                    InputStreamReader isr = new InputStreamReader(System.in);
                    BufferedReader br = new BufferedReader(isr);
            ) {
                try {
                    img1 = args[0];
                    img2 = args[1];
                    if (!new File(img1).exists() || !new File(img2).exists())
                        throw new FileNotFoundException();
                } catch (ArrayIndexOutOfBoundsException | FileNotFoundException ex) {
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
                    img1 = readInput(br, "File path of picture 1:", null, transform);
                    img2 = readInput(br, "File path of picture 2:", null, transform);
                }
            } catch (IOException e) {
                e.printStackTrace();
                System.exit(Main.EXIT_CODE_UNHANDLED_EXCEPTION);
                throw e;
            }

            BufferedImage bi1 = loadImageFromFile(img1);
            BufferedImage bi2 = loadImageFromFile(img2);
            if (bi1.getWidth() != bi2.getWidth() || bi1.getHeight() != bi2.getHeight()) {
                err("Img not same size");
                return;
            }
            BufferedImage bim = new BufferedImage(bi1.getWidth(), bi1.getHeight(), bi1.getType());
            if (bim.getType() != bi1.getType()) {
                err("Different type");
                return;
            }

            int minX = Integer.MAX_VALUE;
            int minY = Integer.MAX_VALUE;
            int maxX = Integer.MIN_VALUE;
            int maxY = Integer.MIN_VALUE;

            int c1, c2;
            for (int y = 0; y < bi1.getHeight(); y++) {
                for (int x = 0; x < bi1.getWidth(); x++) {
                    c1 = bi1.getRGB(x, y);
                    c2 = bi2.getRGB(x, y);
                    if (c1 != c2) {
                        bim.setRGB(x, y, 0xFFFFFF);
                        continue;
                    }
                    bim.setRGB(x, y, c1);

                    minX = Math.min(minX, x);
                    minY = Math.min(minY, y);
                    maxX = Math.max(maxX, x);
                    maxY = Math.max(maxY, y);
                }
            }

            saveImage(bim, "merged");
            info("Saved merged image");

            int w = maxX - minX + 1;
            int h = maxY - minY + 1;

            BufferedImage bic = new BufferedImage(w, h, bi1.getType());
            for (int y = 0; y < h; y++) {
                for (int x = 0; x < w; x++) {
                    bic.setRGB(x, y, bim.getRGB(minX + x, minY + y));
                }
            }

            saveImage(bic, "result");
            info("Saved cropped image as result");
            info("Additional offset: %d, %d", minX, minY);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected String getAppName() {
        return "BH-SamePix";
    }

    @Override
    protected String getScriptFileName() {
        return "samepix";
    }

    @Override
    protected String getUsage() {
        return "<image1> <image2>";
    }

    @Override
    protected String getDescription() {
        return "(developers only) Read from 2 images (must be same size) and yield a new picture with only pixels equally from original pictures";
    }

    @Override
    protected String getLimitationExplain() {
        return null;
    }
}
