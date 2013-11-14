package thundertactics.logic.units;

import thundertactics.comm.mesg.sub.UnitInfo;
import thundertactics.logic.Hero;
import thundertactics.logic.Player;

/**
 * This is the unit that represents the hero on the battle field. It only exists
 * for the length of a fight.
 * 
 * @author Paul Nechifor
 */
public class HeroUnit extends Unit {
    private static class HeroUnitType extends UnitType {
        public final static HeroUnitType INSTANCE = new HeroUnitType();
        
        private HeroUnitType() {
            // None are used, so all are invalid.
            super(-1, -1, -1, -1, -1, -1, -1, -1, null);
        }
        
        @Override
        public HeroUnitType getNewInstance(){
        	throw new RuntimeException("Invalid call");
        }
    }

    private int damage;
    private int defense;
    private int moveRange;
    private int attackRange;
    private int initiative;
    
    private int count;
    private int life;
    
    private Hero hero;
    public HeroUnit(Hero hero) {
        super(HeroUnitType.INSTANCE, 1);
        this.hero = hero;
        damage = hero.getTotalDamage();
        defense = hero.getTotalDefense();
        
        // These should change in the future based on the specialty of the hero
        // like warrior, mage etc.
        moveRange = 3;
        attackRange = hero.getAttackRange();
        
        // Always last.
        initiative = 0;
        
        // Needs a high life because if he dies, the battle is lost.
        life = hero.getTotalLife();
        
        count = 1;
    }
   
    @Override
    public int getTotalDamage() {
        return damage;
    }
   
    @Override
    public int getTotalDefense() {
        return defense;
    }
   
    @Override
    public int getTotalMoveRange() {
        return moveRange;
    }
   
    @Override
    public int getTotalAttackRange() {
        return attackRange;
    }

    @Override
    public int getTotalInitative() {
        return initiative;
    }
    
    @Override
    public int getCount() {
        return count;
    }

    @Override
    public int addHit(int hit) {
        if (hit < life) {
            life -= hit;
            hero.setTotalLife(hero.getTotalLife()-hit);
            hero.updateStats();
            return 0;
        } else {
            life = 0;
            count = 0;
            return 1;
        }
    }
   
    @Override
    public void initalizeLives() {
    }
    
    @Override
    public void deleteLives() {
    }
   
    @Override
    public UnitInfo getUnitInfo() {
       UnitInfo ret = new UnitInfo();

       ret.type = -1;
       ret.damageDelta = damage;
       ret.defenseDelta = defense;
       ret.moveRangeDelta = moveRange;
       ret.attackRangeDelta = attackRange;
       ret.initiativeDelta = initiative;
       ret.count = count;
       
       return ret;
    }
}
