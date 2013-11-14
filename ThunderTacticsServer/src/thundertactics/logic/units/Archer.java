package thundertactics.logic.units;

import thundertactics.logic.IDs;

public class Archer extends UnitType {
    public final static Archer DEFAULT_INSTANCE = new Archer();
    static{
    	DEFAULT_INSTANCE.id = IDs.ARCHER;
    }
    private Archer() {
        super(
            5,   // damage
            1,   // defense
            3,   // moveRange
            6,   // attackRange
            6,  // fullLife
            10,  // initiative
            110, // hireCost
            8,   // leadershipCost
            "archer;arrows;SmallBow"  // appearance
        );
    }
    
    @Override
    public Archer getNewInstance(){
    	return new Archer();
    }
}