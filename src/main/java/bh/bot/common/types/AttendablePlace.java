package bh.bot.common.types;

import bh.bot.common.types.images.BwMatrixMeta;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class AttendablePlace {
    public final String name;
    public final int id;
    public final BwMatrixMeta img;
    public final boolean left;
    public final short procedureTicketMinutes;

    public AttendablePlace(String name, int id, String imgName, boolean left) throws IOException {
        this(name, id, imgName, left, (short) 30);
    }

    public AttendablePlace(String name, int id, String imgCode, boolean left, int procedureTicketMinutes) throws IOException {
        this.name = name;
        this.id = id;
        
        String imgFile;
        switch(imgCode) {
	    	case "pvp":
	    	case "world-boss":
	    	case "raid":
	    	case "invasion":
	    	case "trials":
	    	case "gvg":
            case "quest":
	    	case "gauntlet":
	    	case "expedition":
	        	imgFile = imgCode + "3";
	        	break;
	    	default:
	    		imgFile = imgCode;
	    		break;
        }
        this.img = BwMatrixMeta.from(
                String.format("labels/attendable-places/%s?", imgFile),
                new Offset(0, 0),
                0xFFFFFF
        );
        this.left = left;
        this.procedureTicketMinutes = (short) procedureTicketMinutes;
    }

    public int getId() {
        return id;
    }

    public static class MenuItem {
        public final String name;
        public final int num;

        private MenuItem(String name, int num) {
            this.name = name;
            this.num = num;
        }

        public static MenuItem from(AttendablePlace... attendablePlaces) {
            List<AttendablePlace> aps = Arrays.stream(attendablePlaces).distinct().collect(Collectors.toList());
            int num = 0;
            for (AttendablePlace ap : aps)
                num |= ap.id;
            return new MenuItem(aps.stream().map(x -> x.name).collect(Collectors.joining(" + ")), num);
        }

        public static MenuItem from(String name, AttendablePlace... attendablePlaces) {
            int num = 0;
            for (AttendablePlace ap : Arrays.stream(attendablePlaces).collect(Collectors.toList()))
                num |= ap.id;
            return new MenuItem(name, num);
        }
    }
}
