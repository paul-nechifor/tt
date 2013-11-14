package thundertactics.logic.items;

import thundertactics.comm.mesg.sub.PossessionInfo;
import thundertactics.logic.Hero;

/**
 * @author Paul Nechifor
 */
public abstract class WearableItem extends ItemType {
    public static enum Target {
        HEAD,
        ATTACK_ARM,
        DEFENSE_ARM,
        CHEST,
        LEGS,
        HERO;
        
        public static final int TOTAL = 5;
    }
    
    protected Target[] targets;
    
    /**
     * The actions performed when it is equipped, like increasing the armor for
     * a shield.
     */
    public abstract void equip(Hero hero);
    
    /**
     * The actions performed when it is unequipped, like decreasing the armor
     * for a shield.
     */
    public abstract void unequip(Hero hero);
    
	public boolean canEquip(Hero h){
		return true;
	}
    
    public Target[] getTargets() {
        return targets;
    }
}
