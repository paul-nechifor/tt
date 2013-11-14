package thundertactics.comm.mesg.to;

import thundertactics.comm.mesg.from.MoveFrom;
import thundertactics.comm.mesg.sub.MoveResultsInfo;

public final class MoveAndResultsTo extends MesgTo {
    public MoveFrom move;
    public MoveResultsInfo results;
}
