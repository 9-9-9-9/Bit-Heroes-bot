package bh.bot.common.types;

import bh.bot.common.Configuration;
import bh.bot.common.types.images.BwMatrixMeta;
import bh.bot.common.utils.ImageUtil;

import java.io.IOException;

public class AttendablePlace {
    public final String name;
    public final int id;
    public final BwMatrixMeta img;
    public final boolean left;

    public AttendablePlace(String name, int id, String imgName, boolean left) throws IOException {
        this.name = name;
        this.id = id;
        this.img = new BwMatrixMeta(
                ImageUtil.loadImageFileFromResource(String.format("labels/attendable-places/%s", imgName)),
                new Configuration.Offset(0, 0),
                0xFFFFFF
        );
        this.left = left;
    }

    public int getId() {
        return id;
    }
}
