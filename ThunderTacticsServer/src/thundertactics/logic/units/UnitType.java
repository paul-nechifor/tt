package thundertactics.logic.units;

import java.util.HashMap;

import thundertactics.comm.mesg.sub.PossessionInfo;
import thundertactics.logic.Ownable;

/**
 * Unit types are modeled as subclasses of this one (as opposed to having a list
 * of properties) because they might have associated methods in the future (like
 * unit specific actions).
 * 
 * @author Paul Nechifor
 */
public abstract class UnitType extends Ownable {
	
    protected int damage;
    protected int defense;
    protected int moveRange;
    protected int attackRange;
    protected int fullLife;
    protected int initiative;
    protected int hireCost;
    protected int leadershipCost;

    protected String appearance;
    
    public UnitType(int damage, int defense, int moveRange, int attackRange,
            int fullLife, int initiative, int hireCost, int leadershipCost,
            String appearance) {
        this.damage = damage;
        this.defense = defense;
        this.moveRange = moveRange;
        this.attackRange = attackRange;
        this.fullLife = fullLife;
        this.initiative = initiative;
        this.hireCost = hireCost;
        this.leadershipCost = leadershipCost;
        this.appearance = appearance;
    }
    
    public int getDamage() {
        return damage;
    }
    
    public int getDefense() {
        return defense;
    }
    
    public int getMoveRange() {
        return moveRange;
    }
    
    /**
     * Returns the attack range. If it is 1, that means this is a non-ranged
     * unit (like a swordsman) and his range shouldn't ever be extended.
     */
    public int getAttackRange() {
        return attackRange;
    }
    
    public int getFullLife() {
        return fullLife;
    }
    
    public int getInitiative() {
        return initiative;
    }
    
    public int getHireCost() {
        return hireCost;
    }
    
    public int getLeadershipCost() {
        return leadershipCost;
    }
    
    public String getAppearance() {
        return appearance;
    }
    
    public abstract UnitType getNewInstance();
    
    public void fill(PossessionInfo pi){
    	pi.p = new HashMap<String, String>();
    	/*if(damage!=0)
    	pi.p.put("Damage", Integer.toString(damage));
    	if(defense!=0)
    	pi.p.put("Defense", Integer.toString(defense));
    	if(moveRange!=0)
    	pi.p.put("Move range", Integer.toString(moveRange));
    	if(attackRange!=0)
    	pi.p.put("Attack range", Integer.toString(attackRange));
    	if(fullLife!=0)
    	pi.p.put("Life", Integer.toString(fullLife));
    	if(initiative!=0)
    	pi.p.put("Initiative", Integer.toString(initiative));
    	if(hireCost!=0)
    	pi.p.put("Hire cost", Integer.toString(hireCost));
    	if(leadershipCost!=0)
    	pi.p.put("Leadership cost", Integer.toString(leadershipCost));*/
    }
}
