package thundertactics.logic.items.weapons;

import java.util.HashMap;

import thundertactics.comm.mesg.sub.PossessionInfo;
import thundertactics.logic.Hero;
import thundertactics.logic.IDs;
import thundertactics.logic.items.WearableItem;
import thundertactics.logic.items.WearableItem.Target;
import thundertactics.logic.items.shields.SmallShield;

public class SmallBow extends WearableItem{
    public static final SmallBow DEFAULT_INSTANCE = new SmallBow();
    static{
    	DEFAULT_INSTANCE.id = IDs.SMALLBOW;
    }
    private int damage, range;
    
    private SmallBow() {
        this.name = "Small bow";
        this.damage = 10;
        this.range = 8;
        this.defaultValue = 300;
        // Two handed..
        this.targets = new Target[]{WearableItem.Target.ATTACK_ARM, WearableItem.Target.DEFENSE_ARM};
        this.object3dAppearance = "SmallBow;arrows";
    }
	
	@Override
	public void equip(Hero hero) {
		hero.addDamage(damage);
		hero.setAttackRange(range);
	}

	@Override
	public void unequip(Hero hero) {
		hero.addDamage(- damage);
		hero.setAttackRange(1);
	}
    
    @Override
    public SmallBow getNewInstance(){
    	return new SmallBow();
    }
    public void fill(PossessionInfo pi){
    	super.fill(pi);
    	pi.p.put("Damage", Integer.toString(damage));
    	pi.p.put("Attack range", Integer.toString(range));
    }

}
