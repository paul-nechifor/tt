package thundertactics.comm.mesg.to;

import java.util.List;
import thundertactics.comm.mesg.sub.PossessionInfo;

public class ShopPossessionsTo extends MesgTo {
	/**
	 * Shop id
	 */
	public int i;
    public String name;
    public List<PossessionInfo> items;
    public List<PossessionInfo> units;
}
