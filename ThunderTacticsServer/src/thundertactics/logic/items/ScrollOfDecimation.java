package thundertactics.logic.items;

import thundertactics.comm.mesg.sub.PossessionInfo;
import thundertactics.logic.IDs;

public class ScrollOfDecimation extends Scroll {
    public static final ScrollOfDecimation DEFAULT_INSTANCE = new ScrollOfDecimation();
    static{
    	DEFAULT_INSTANCE.id = IDs.SROLLOFDECIMATION;
    }
    
    private ScrollOfDecimation() {
        this.name = "Scroll of Decimation";
        this.htmlDescription = "Kills one in ten troops of the selected unit.";
        this.defaultValue = 100;
        this.target = Scroll.Target.UNIT;
    }
    
    @Override
    public void performAction() {
        // TODO Auto-generated method stub
    }
    
    @Override
    public ScrollOfDecimation getNewInstance(){
    	return new ScrollOfDecimation();
    }
    public void fill(PossessionInfo pi){
    	super.fill(pi);
    	pi.p.put("Effect", htmlDescription);
    }
}
