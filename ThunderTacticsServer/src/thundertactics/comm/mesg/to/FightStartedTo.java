package thundertactics.comm.mesg.to;

import thundertactics.comm.mesg.sub.FightOpponentInfo;

public final class FightStartedTo extends MesgTo {
    /**
     * Contains the key of the FightScene.
     */
    public String scene;
    
    /**
     * All the fighters in the order of move.
     */
    public FightOpponentInfo[] fighters;
}
