package thundertactics.logic.units;

import thundertactics.logic.IDs;

public class DarkKnight extends UnitType {
    public final static DarkKnight DEFAULT_INSTANCE = new DarkKnight();
    static{
    	DEFAULT_INSTANCE.id = IDs.DARKKNIGHT;
    }
    
    private DarkKnight() {
        super(
            20,  // damage
            4,   // defense
            5,   // moveRange
            1,   // attackRange
            35,  // fullLife
            20,  // initiative
            200, // hireCost
            30,  // leadershipCost
            "swordman,swordsman4;Scythe"  // appearance
        );
    }
    @Override
    public DarkKnight getNewInstance(){
    	return new DarkKnight();
    }
}