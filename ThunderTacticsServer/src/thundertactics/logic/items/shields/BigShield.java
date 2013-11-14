package thundertactics.logic.items.shields;

import java.util.HashMap;

import thundertactics.comm.mesg.sub.PossessionInfo;
import thundertactics.logic.Hero;
import thundertactics.logic.IDs;
import thundertactics.logic.items.CrownOfLeader;
import thundertactics.logic.items.ScrollOfDivineIntervention;
import thundertactics.logic.items.WearableItem;

public class BigShield extends WearableItem{
    public static final BigShield DEFAULT_INSTANCE = new BigShield();
    static{
    	DEFAULT_INSTANCE.id = IDs.BIGSHIELD;
    }
    private int defense;
    
    private int minLevel;
    private int minDefense;
    private BigShield() {
        this.name = "Big shield";
        this.defense = 50;
        minLevel = 5;
        minDefense = 40;
        
        this.htmlDescription = String.format("Effects:<br/>Defense %d",defense);
        this.defaultValue = 300;
        this.targets = new Target[]{WearableItem.Target.DEFENSE_ARM};
        this.object3dAppearance = "BigShield";
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
    public BigShield getNewInstance(){
    	return new BigShield();
    }
    public boolean canEquip(Hero h){
    	return h.getLevel()>= minLevel && h.getBaseDefense()>= minDefense;
    }
    public void fill(PossessionInfo pi){
    	super.fill(pi);
    	pi.p.put("Defense", Integer.toString(defense));
    	if(pi.r==null) pi.r = new HashMap<String, String>();
    	pi.r.put("Level", Integer.toString(minLevel));
    	pi.r.put("Defense", Integer.toString(minDefense));
    }
}
