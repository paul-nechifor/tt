package thundertactics.logic.items;

import thundertactics.comm.mesg.sub.PossessionInfo;
import thundertactics.logic.Hero;
import thundertactics.logic.IDs;

/**
 * @author Paul Nechifor
 */
public class CrownOfLeader extends WearableItem {
	//FIXME: why single instance? what if in future we let users to improve their items with other items.
    public static final CrownOfLeader DEFAULT_INSTANCE = new CrownOfLeader();
    static{
    	DEFAULT_INSTANCE.id = IDs.CROWNOFLEADER;
    }
    private int defenseAddition = 10;
    private int leadershipAddition = 1000;
    private CrownOfLeader() {
        this.name = "Crown of Leader";
        this.defaultValue = 300;
        this.targets = new Target[]{WearableItem.Target.HEAD};
    }

    @Override
    public void equip(Hero hero) {
        hero.addDefense(defenseAddition);
        hero.addLeadership(leadershipAddition);
    }

    @Override
    public void unequip(Hero hero) {
    	// Actually subtract..
        hero.addDefense(- defenseAddition);
        hero.addLeadership(- leadershipAddition);
    }
    
    @Override
    public CrownOfLeader getNewInstance(){
    	return new CrownOfLeader();
    }
}
