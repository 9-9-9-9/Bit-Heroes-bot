package bh.bot.app;

import bh.bot.Main;
import bh.bot.common.Log;
import bh.bot.common.types.tuples.Tuple3;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Function;

public class KeepPixApp extends AbstractApplication {
    @Override
    protected void internalRun(String[] args) {
        try {
            throwNotSupportedFlagExit(launchInfo.exitAfterXSecs);

            if (args.length != 0 && args.length != 2) {
                Log.info("Invalid number of arguments");
                Log.info(getHelp());
                System.exit(Main.EXIT_CODE_INVALID_NUMBER_OF_ARGUMENTS);
                return;
            }

            String templateImg, inputImg;
            try {
                templateImg = args[0];
                inputImg = args[1];
                if (!new File(templateImg).exists() || !new File(inputImg).exists())
                    throw new FileNotFoundException();
            } catch (ArrayIndexOutOfBoundsException | FileNotFoundException ex) {
                Log.info(getHelp());
                Function<String, Tuple3<Boolean, String, String>> transform = s -> {
                    try{
                        File file = new File(s);
                        if (!file.exists())
                            return new Tuple3<>(false, "File does not exists", null);
                        return new Tuple3<>(true, null, s);
                    } catch (Exception ex2) {
                        return new Tuple3<>(false, "Invalid file path", null);
                    }
                };
                templateImg = readInput("File path of the template picture you want to get source pixels:", null, transform);
                inputImg = readInput("File path of the picture you want to process:", null, transform);
            }

            BufferedImage biTemplate = loadImageFromFile(templateImg);
            BufferedImage biInput = loadImageFromFile(inputImg);
            BufferedImage biOutput = new BufferedImage(biInput.getWidth(), biInput.getHeight(), biInput.getType());

            Set<Integer> templatePixels = new HashSet<>();
            for (int y = 0; y < biTemplate.getHeight(); y++)
                for (int x = 0; x < biTemplate.getWidth(); x++)
                    templatePixels.add(biTemplate.getRGB(x, y));

            int minX = Integer.MAX_VALUE;
            int minY = Integer.MAX_VALUE;
            int maxX = Integer.MIN_VALUE;
            int maxY = Integer.MIN_VALUE;
            for (int y = 0; y < biInput.getHeight(); y++)
                for (int x = 0; x < biInput.getWidth(); x++)
                {
                    int rgb = biInput.getRGB(x, y);
                    if (templatePixels.contains(rgb))
                    {
                        biOutput.setRGB(x, y, rgb);

                        minX = Math.min(minX, x);
                        minY = Math.min(minY, y);
                        maxX = Math.max(maxX, x);
                        maxY = Math.max(maxY, y);
                    }
                    else
                        biOutput.setRGB(x, y, 0xFFFFFF);
                }

            saveImage(biOutput, "filtered");
            Log.info("Saved filtered image");
            
            int w = maxX - minX + 1;
            int h = maxY - minY + 1;

            Log.info("Cropped size: %dx%d from original %d, %d", w, h, biOutput.getWidth(), biOutput.getHeight());

            BufferedImage bic = new BufferedImage(w, h, biInput.getType());
            for (int y = 0; y < h; y++) {
                for (int x = 0; x < w; x++) {
                    try {
                        bic.setRGB(x, y, biOutput.getRGB(minX + x, minY + y));
                    } catch (ArrayIndexOutOfBoundsException ex) {
                        ex.printStackTrace();
                        Log.err("Failure cord = %d,%d from %d, %d", x, y, minX + x, minY + y);
                    }
                }
            }

            saveImage(bic, "result");
            Log.info("Saved cropped image as result");

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public String getAppCode() {
        return "keeppix";
    }

    @Override
    protected String getAppName() {
        return "BH-KeepPix";
    }

    @Override
    protected String getScriptFileName() {
        return "keeppix";
    }

    @Override
    protected String getUsage() {
        return "<source> <input>";
    }

    @Override
    protected String getDescription() {
        return "(developers only) Read 2 images: source and input, filter pixels from the input image, only keep pixels which exists in source image";
    }

    @Override
    protected String getFlags() {
        return null;
    }

    @Override
    protected String getLimitationExplain() {
        return null;
    }
}
