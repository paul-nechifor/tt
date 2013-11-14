package thundertactics.logic.units;

import thundertactics.logic.IDs;

public class Guardsman extends UnitType {
    public final static Guardsman DEFAULT_INSTANCE = new Guardsman();
    static{
    	DEFAULT_INSTANCE.id = IDs.GUARDSMAN;
    }
    private Guardsman() {
        super(
            8,   // damage
            2,   // defense
            4,   // moveRange
            1,   // attackRange
            10,  // fullLife
            12,  // initiative
            80,  // hireCost
            10,  // leadershipCost
            "swordman;SmallSword"  // appearance
        );
    }
    @Override
    public Guardsman getNewInstance(){
    	return new Guardsman();
    }
}