package thundertactics.logic.units;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import thundertactics.comm.mesg.sub.UnitInfo;

/**
 * The class that represents a unit.
 * 
 * @author Paul Nechifor
 */
public class Unit implements Serializable {
    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private UnitType type;
    
    private transient int damageDelta;
    private transient int defenseDelta;
    private transient int moveRangeDelta;
    private transient int attackRangeDelta;
    private transient int initiativeDelta;
    
    private int count;
    
    /**
     * This is initialized when a fight starts and deleted afterwards.
     */
    private transient List<Integer> lives;
    
    public Unit(UnitType type, int count) {
         this.type = type;
         this.count = count;
    }
    
    public UnitType getType() {
        return type;
    }
    
    public int getTotalDamage() {
        return Math.max(0, count * (type.getDamage() + damageDelta));
    }
    
    public int getTotalDefense() {
        return Math.max(0, count * (type.getDefense() + defenseDelta));
    }
    
    public int getTotalMoveRange() {
        return Math.max(1, type.getMoveRange() + moveRangeDelta);
    }
    
    public int getTotalAttackRange() {
        return Math.max(1, type.getAttackRange() + attackRangeDelta);
    }

    public int getTotalInitative() {
        return Math.max(0, count * (type.getInitiative() + initiativeDelta));
    }
    
    public int getCount() {
        return count;
    }
    
    /**
     * Distributes the hit randomly upon the units and returns the number that
     * died.
     */
    public int addHit(int hit) {
        int hitLeft = hit;
        
        // Subtracting the defended part.
        hitLeft -= getTotalDefense();
        if (hitLeft <= 0) {
            return 0;
        }
        
        int incHit = (int) Math.ceil(3 * (hit / (double) count));
        int addHit;
        int which;
        int life;
        int died = 0;
        
        while (hitLeft > 0 && count > 0) {
            addHit = (int) (Math.random() * incHit) + 1;
            if (addHit > hitLeft) {
                addHit = hitLeft;
            }
            
            which = (int) (Math.random() * count);
            life = lives.get(which);
            if (life > addHit) {
                lives.set(which, life - addHit);
                hitLeft -= addHit;
            } else {
                hitLeft -= life;
                lives.remove(which);
                count--;
                died++;
            }
        }
        
        return died;
    }
    
    public void initalizeLives() {
        lives = new ArrayList<Integer>(count);
        for (int i = 0; i < count; i++) {
            lives.add(type.getFullLife());
        }
    }
    
    public void deleteLives() {
        lives.clear();
        lives = null;
    }
    
    public UnitInfo getUnitInfo() {
        UnitInfo ret = new UnitInfo();

        ret.type = type.getId();
        ret.damageDelta = damageDelta;
        ret.defenseDelta = defenseDelta;
        ret.moveRangeDelta = moveRangeDelta;
        ret.attackRangeDelta = attackRangeDelta;
        ret.initiativeDelta = initiativeDelta;
        ret.count = count;
        
        return ret;
    }

	public void setCount(int i) {
		this.count = i;
	}
}
