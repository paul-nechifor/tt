package thundertactics.logic.items.weapons;

import java.util.HashMap;

import thundertactics.comm.mesg.sub.PossessionInfo;
import thundertactics.logic.Hero;
import thundertactics.logic.IDs;
import thundertactics.logic.items.WearableItem;

public class Scythe extends WearableItem{
    public static final Scythe DEFAULT_INSTANCE = new Scythe();
    static{
    	DEFAULT_INSTANCE.id = IDs.SCYTHE;
    }
    private int damage, defense;
    
    private int minLevel;
    private int minDamage;
    
    private Scythe() {
        this.name = "Scythe";
        this.damage = 100;
        this.defense = 30;
        minLevel = 5;
        minDamage = 35;
        this.defaultValue = 300;
        this.targets = new Target[]{WearableItem.Target.ATTACK_ARM, WearableItem.Target.DEFENSE_ARM};
        this.object3dAppearance = "Scythe";
    }
	
	@Override
	public void equip(Hero hero) {
		hero.addDamage(damage);
		hero.addDefense(defense);
	}

	@Override
	public void unequip(Hero hero) {
		hero.addDamage(- damage);
		hero.addDefense(- defense);
		hero.setAttackRange(1);
	}

    @Override
    public Scythe getNewInstance(){
    	return new Scythe();
    }
    public void fill(PossessionInfo pi){
    	super.fill(pi);
    	pi.p.put("Damage", Integer.toString(damage));
    	pi.p.put("Defense", Integer.toString(defense));
    	pi.p.put("Attack range", "1");
    	if(pi.r==null) pi.r = new HashMap<String, String>();
    	pi.r.put("Level",Integer.toString(minLevel));
    	pi.r.put("Damage",Integer.toString(minDamage));
    }
    
    public boolean canEquip(Hero h){
    	return minLevel<=h.getLevel() && minDamage<=h.getBaseDamage();
    }
}
