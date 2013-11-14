package thundertactics.logic.units;

import thundertactics.logic.IDs;

public class LongBowman extends UnitType {
    public final static LongBowman DEFAULT_INSTANCE = new LongBowman();
    static{
    	DEFAULT_INSTANCE.id = IDs.LONGBOWMAN;
    }
    
    private LongBowman() {
        super(
            15,  // damage
            2,   // defense
            4,   // moveRange
            10,  // attackRange
            25,  // fullLife
            15,  // initiative
            270, // hireCost
            35,  // leadershipCost
            "archer,archer3;arrows;SmallBow"  // appearance
        );
    }
    
    @Override
    public LongBowman getNewInstance(){
    	return new LongBowman();
    }
}