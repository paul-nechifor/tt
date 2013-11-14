package thundertactics.logic.items.potions;

import thundertactics.comm.mesg.sub.PossessionInfo;
import thundertactics.logic.Hero;
import thundertactics.logic.IDs;

public class PotionOfLife extends Potion{
    public static final PotionOfLife DEFAULT_INSTANCE = new PotionOfLife();
    static{
    	DEFAULT_INSTANCE.id = IDs.POTIONOFLIFE;
    }
    private int life;
    
    private PotionOfLife() {
        this.name = "Potion of life";
        this.defaultValue = 100;
        this.count=10;
        this.targets = new Target[]{Potion.Target.HERO};
        this.life = 100;
        this.object3dAppearance = "PotionOfLife";
    }
	
	@Override
	public void use(Hero hero) {
		if(count<=0) return;
		count--;
		hero.addLife(life);
	}
    
    @Override
    public PotionOfLife getNewInstance(){
    	return new PotionOfLife();
    }

    public void fill(PossessionInfo pi){
    	super.fill(pi);
    	pi.p.put("Life heal", Integer.toString(life));
    	pi.p.put("Count", Integer.toString(count));
	}

	public Target[] getTargets() {
		return this.targets;
	}
}
