package thundertactics.logic.fight;

import java.util.ArrayList;
import java.util.List;
import thundertactics.logic.Hero;
import thundertactics.logic.units.HeroUnit;
import thundertactics.logic.units.Unit;

public class TrustedFighter {
    private final IFighter fighter;
    private final int index;
    private final ArrayList<TrustedUnit> units = new ArrayList<TrustedUnit>();
    private TrustedUnit hero;
    private int leftToMove = -1;
    
    public TrustedFighter(IFighter fighter, int index, byte[][] pos,
            SceneObject[][] map) {
        this.fighter = fighter;
        this.index = index;
        
        Unit[] fighterUnits = fighter.getHero().getBattleReadyUnits();
        int nUnits = Hero.ACTIVE_UNITS + 1;
        byte x, y;
        
        for (int i = 0; i < nUnits; i++) {
            if (fighterUnits[i] != null) {
                x = pos[i][0];
                y = pos[i][1];
                TrustedUnit u = new TrustedUnit(this, fighterUnits[i], x, y);
                u.getUnit().initalizeLives();
                units.add(u);
                map[x][y] = u;
                if(u.getUnit() instanceof HeroUnit) this.hero = u;
            }
        }
        units.trimToSize();
    }
    public TrustedUnit getHero(){
    	return hero;
    }
    public final void removeUnit(TrustedUnit unit, SceneObject[][] map) {
        unit.getUnit().deleteLives();
        units.remove(unit);
        map[unit.getX()][unit.getY()] = null;
    }
    
    public final void terminateFighter(SceneObject[][] map) {
        for (TrustedUnit u : units) {
            u.getUnit().deleteLives();
            map[u.getX()][u.getY()] = null;
        }
        units.clear();
        leftToMove = 0;
    }
    
    public final IFighter getFighter() {
        return fighter;
    }
    
    public final int getIndex() {
        return index;
    }
    
    public final int getLeftToMove() {
        return leftToMove;
    }
    
    public List<TrustedUnit> getUnits() {
        return units;
    }
    
    public void prepareForTurn() {
        leftToMove = units.size();
        for (TrustedUnit u : units) {
            u.setToMovable();
        }
    }
    
    public void madeValidUnitMove() {
        leftToMove--;
    }
}
