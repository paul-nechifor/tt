package thundertactics.logic.units;

import thundertactics.logic.IDs;

public class Knight extends UnitType {
    public final static Knight DEFAULT_INSTANCE = new Knight();
    static{
    	DEFAULT_INSTANCE.id = IDs.KNIGHT;
    }
    
    private Knight() {
        super(
            12,  // damage
            3,  // defense
            3,   // moveRange
            1,   // attackRange
            45,  // fullLife
            12,  // initiative
            120, // hireCost
            20,  // leadershipCost
            "swordman,swordsman6;BigShield;SmallSword"  // appearance
        );
    }
    
    @Override
    public Knight getNewInstance(){
    	return new Knight();
    }
}