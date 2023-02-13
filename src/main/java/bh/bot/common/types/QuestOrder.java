package bh.bot.common.types;

public final class QuestOrder {
   public final char Dungeons = 'D';
   public final char FilledStars = 'S';
   public final char EmptyStars = 'E';
   public final char Flags = 'F';

   public final String defaultOrder = "" + Dungeons + FilledStars + EmptyStars + Flags;
}
