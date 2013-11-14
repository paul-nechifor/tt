package thundertactics.logic;

import thundertactics.comm.mesg.sub.PossessionInfo;

public class Possession<T extends Ownable> {
    private final T owned;
    private int cost;
    
    public Possession(T owned, int cost) {
        this.owned = owned;
        this.cost = cost;
    }
    
    public final T getOwned() {
        return owned;
    }
    
    public void setCost(int cost) {
        this.cost = cost;
    }
    
    public int getCost() {
        return cost;
    }

	public void fill(PossessionInfo possessionInfo) {
		possessionInfo.i = owned.getId();
		possessionInfo.c = getCost();
		owned.fill(possessionInfo);
	}
}
