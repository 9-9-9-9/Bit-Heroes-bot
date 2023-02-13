package bh.bot.common.types;

public final class QuestOrder {
   public final static char Dungeons = 'D';
   public final static char FilledStars = 'S';
   public final static char EmptyStars = 'E';
   public final static char Flags = 'F';

   public final static String defaultOrder = "" + Dungeons + FilledStars + EmptyStars + Flags;
}
