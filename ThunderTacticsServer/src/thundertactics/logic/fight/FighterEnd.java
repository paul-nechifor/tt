package thundertactics.logic.fight;

import thundertactics.comm.mesg.to.FighterEndTo;

public class FighterEnd {
    public static final int WON = 1;
    public static final int LOST = 2;
    public static final int PEACE = 3;
    public static final int KICKED = 4;
    
    private final IFighter fighter;
    private final int type;
    private final String kickedReason;
    
    public FighterEnd(IFighter fighter, int type, String kickedReason) {
        this.fighter = fighter;
        this.type = type;
        this.kickedReason = kickedReason;
    }

    public final FighterEndTo getFighterEndTo() {
        FighterEndTo ret = new FighterEndTo();
        ret.fighter = fighter.getHero().getName();
        ret.type = type;
        ret.kickedReason = kickedReason;
        return ret;
    }
    
    public final IFighter getFighter() {
        return fighter;
    }
    
    public final int getType() {
        return type;
    }
    
    public final String getKickedReason() {
        return kickedReason;
    }
    
    public final boolean isPositiveEnd() {
        return type == WON || type == PEACE;
    }
}
