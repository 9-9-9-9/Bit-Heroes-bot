package bh.bot.common.types;

import bh.bot.common.Configuration;

import java.awt.*;

public class Offset {
    public final int X;
    public final int Y;

    public Offset(int x, int y) {
        this.X = x;
        this.Y = y;
    }

    public Point toScreenCoordinate() {
        return new Point(Configuration.gameScreenOffset.X.get() + X, Configuration.gameScreenOffset.Y.get() + Y);
    }

    @Override
    public String toString() {
        return String.format("%s[x=%d,y=%d]", this.getClass().getName(), X, Y);
    }

    public static Offset fromKeyPrefix(String keyPrefix) {
        int x = Configuration.readInt(keyPrefix + ".x");
        int y = Configuration.readInt(keyPrefix + ".y");
        if (x < 0)
            throw new IllegalArgumentException(
                    String.format("Value of offset %s.x can not be a negative number: %d", keyPrefix, x));
        if (y < 0)
            throw new IllegalArgumentException(
                    String.format("Value of offset %s.y can not be a negative number: %d", keyPrefix, y));
        return new Offset(x, y);
    }

    public static Offset none() {
        return new Offset(-1, -1);
    }
}
