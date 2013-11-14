package thundertactics.logic.fight;

import java.awt.Point;
import java.util.ArrayList;
import java.util.List;
import thundertactics.comm.mesg.sub.MoveResultsInfo;
import thundertactics.logic.units.Unit;

/**
 * This class codifies the results of a {@link Move} as calculated by the
 * server.
 * 
 * TODO: So far only deaths and damage are codified. Healing of units and others
 * must be taken into account.
 * 
 * @author Paul Nechifor
 */
public final class MoveResults {
    public static final class Damage {
        private final Point unitCell;
        private final int died;
        private final int damage;
        
        public Damage(Point unitCell, int died, int damage) {
            this.unitCell = unitCell;
            this.died = died;
            this.damage = damage;
        }
        
        /**
         * The position of the {@link Unit} (after performed actions).
         */
        public final Point getUnitCell() {
            return unitCell;
        }
        
        /**
         * The number of troops from that {@link Unit} which died. This doesn't
         * necessarily imply damage as this might have been an effect of magic.
         */
        public final int getDied() {
            return died;
        }
        
        /**
         * The total amount of damage which that {@link Unit} has suffered.
         */
        public final int getDamage() {
            return damage;
        }
    }

    private final List<Damage> damages = new ArrayList<Damage>();
    
    public MoveResults() {
    }
    
    /**
     * This lists the death count and damage for any unit that has been
     * affected. Note that, at any turn, this can include any unit from any
     * fighter, as there are multiple reasons for dying and suffering damage.
     * 
     * @return A list of units, with associated dead count and damage, but only
     * for units which have been affected.
     */
    public final List<Damage> getDamages() {
        return damages;
    }
    
    public final void addDamage(Damage d) {
        damages.add(d);
    }
    
    /**
     * Returns a conversion to {@link MoveResultsInfo}.
     */
    public final MoveResultsInfo toMoveResultsInfo() {
        MoveResultsInfo mesg = new MoveResultsInfo();
        mesg.damages = damages;
        return mesg;
    }
}
