package thundertactics.logic.items.helmets;

import java.util.HashMap;

import thundertactics.comm.mesg.sub.PossessionInfo;
import thundertactics.logic.Hero;
import thundertactics.logic.IDs;
import thundertactics.logic.items.CrownOfLeader;
import thundertactics.logic.items.ScrollOfDivineIntervention;
import thundertactics.logic.items.WearableItem;

public class VikingHelmet extends WearableItem{
    public static final VikingHelmet DEFAULT_INSTANCE = new VikingHelmet();
    static{
    	DEFAULT_INSTANCE.id = IDs.VIKINGHELMET;
    }
    private int defense;
    
    private int minLevel;
    private int minDefense;
    private VikingHelmet() {
        this.name = "Viking Helmet";
        this.defense = 50;
        minLevel = 5;
        minDefense = 40;
        this.defaultValue = 300;
        this.targets = new Target[]{WearableItem.Target.HEAD};
        this.object3dAppearance = "VikingHelmet";
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
    public VikingHelmet getNewInstance(){
    	return new VikingHelmet();
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
