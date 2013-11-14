package thundertactics.logic.items.shields;

import thundertactics.comm.mesg.sub.PossessionInfo;
import thundertactics.logic.Hero;
import thundertactics.logic.IDs;
import thundertactics.logic.items.CrownOfLeader;
import thundertactics.logic.items.ScrollOfDivineIntervention;
import thundertactics.logic.items.WearableItem;

public class SmallShield extends WearableItem{
    public static final SmallShield DEFAULT_INSTANCE = new SmallShield();
    static{
    	DEFAULT_INSTANCE.id = IDs.SMALLSHIELD;
    }
    private int defense;
    
    private SmallShield() {
        this.name = "Small shield";
        this.defense = 10;
        this.htmlDescription = String.format("Effects:<br/>Defense %d",defense);
        this.defaultValue = 300;
        this.targets = new Target[]{WearableItem.Target.DEFENSE_ARM};
        this.object3dAppearance = "SmallShield";
    }
	
	@Override
	public void equip(Hero hero) {
		hero.addDefense(defense);
	}

	@Override
	public void unequip(Hero hero) {
		hero.addDefense(- defense);
	}
    
    @Override
    public SmallShield getNewInstance(){
    	return new SmallShield();
    }
    public void fill(PossessionInfo pi){
    	super.fill(pi);
    	pi.p.put("Defense", Integer.toString(defense));
    }
}
