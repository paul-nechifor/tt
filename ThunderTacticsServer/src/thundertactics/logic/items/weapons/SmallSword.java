package thundertactics.logic.items.weapons;

import thundertactics.comm.mesg.sub.PossessionInfo;
import thundertactics.logic.Hero;
import thundertactics.logic.IDs;
import thundertactics.logic.items.WearableItem;
import thundertactics.logic.items.WearableItem.Target;

public class SmallSword extends WearableItem{
    public static final SmallSword DEFAULT_INSTANCE = new SmallSword();
    static{
    	DEFAULT_INSTANCE.id = IDs.SMALLSWORD;
    }
    private int damage, defense;
    
    private SmallSword() {
        this.name = "Some Sword";
        this.damage = 10;
        this.defense = 10;
        this.defaultValue = 300;
        this.targets = new Target[]{WearableItem.Target.ATTACK_ARM};
        this.object3dAppearance = "SmallSword";
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
    public SmallSword getNewInstance(){
    	return new SmallSword();
    }
    public void fill(PossessionInfo pi){
    	super.fill(pi);
    	pi.p.put("Damage", Integer.toString(damage));
    	pi.p.put("Defense", Integer.toString(defense));
    	pi.p.put("Attack range", "1");
    }
}
