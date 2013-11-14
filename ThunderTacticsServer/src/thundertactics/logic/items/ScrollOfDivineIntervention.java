package thundertactics.logic.items;

import thundertactics.logic.IDs;

public class ScrollOfDivineIntervention extends Scroll {
    public static final ScrollOfDivineIntervention DEFAULT_INSTANCE =
            new ScrollOfDivineIntervention();
    static{
    	DEFAULT_INSTANCE.id = IDs.SCROLLOFDIVINEINTERVENTION;
    }
    
    private ScrollOfDivineIntervention() {
        this.name = "Scroll of Divine Intervention";
        this.htmlDescription = "Heals all your units instantly.";
        this.defaultValue = 1000;
        this.target = Scroll.Target.UNIT;
    }
    
    @Override
    public void performAction() {
        // TODO Auto-generated method stub
    }
    
    @Override
    public ScrollOfDivineIntervention getNewInstance(){
    	return new ScrollOfDivineIntervention();
    }
}