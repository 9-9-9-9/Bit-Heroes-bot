package bh.bot.common.types;

import java.util.concurrent.atomic.AtomicInteger;

public class AtomicOffset {
    public final AtomicInteger X;
    public final AtomicInteger Y;

    public AtomicOffset(int x, int y) {
        this.X = new AtomicInteger(x);
        this.Y = new AtomicInteger(y);
    }

    public AtomicOffset(Offset offset) {
        this(offset.X, offset.Y);
    }

    public void set(int x, int y) {
        this.X.set(x);
        this.Y.set(y);
    }

    public void set(Offset offset) {
        this.set(offset.X, offset.Y);
    }
}
