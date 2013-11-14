package thundertactics.logic.fight;

import java.awt.Point;
import thundertactics.comm.mesg.from.MoveFrom;
import thundertactics.logic.items.Scroll;
import thundertactics.logic.units.HeroUnit;

/**
 * This class codifies a move. The validity of a move can only be verified
 * within the context of a {@link Fight}. Only some fields are used for a given
 * move, and incompatibilities between them are specified.
 * 
 * Moves work like this: when it is the turn of a certain {@link IFighter}, he
 * performs a series of actions for every one of his units (in no particular
 * order) and then his turn is over. Each series of actions is codified as a
 * {@link Move}, and for every {@link Move} there is a {@link MoveResults}.
 * 
 * A unit can perform the following actions:
 * <ul>
 * <li>pass, i.e. do nothing (this cannot be used with any other action);</li>
 * <li>move to another cell within range;</li>
 * <li>perform attack on other unit within range;</li>
 * <li>cast spell/scroll on self or on target within range.</li>
 * </ul>
 * 
 * Given that the hero holds the scrolls, only the unit which represents the
 * hero (i.e. {@link HeroUnit}) can use a {@link Scroll}.
 * 
 * A unit can go in 8 directions (codified as 0 to 7). Up is represented by 0,
 * and they go clockwise.
 * 
 * @author Paul Nechifor
 */
public final class Move {
    private final Point unit;
    private final boolean defend;
    private final byte[] movement;
    private final Point attack;
    private final Scroll scroll;
    private final Point scrollTarget;
    private final MoveFrom origin;
    
    public Move(Point unit, boolean defend, byte[] movement, Point attack,
            Scroll scroll, Point scrollTarget, MoveFrom origin) {
        this.unit = unit;
        this.defend = defend;
        this.movement = movement;
        this.attack = attack;
        this.scroll = scroll;
        this.scrollTarget = scrollTarget;
        this.origin = origin;
    }
    
    /**
     * The unit which performs an action in this move.
     * 
     * @return The cell position of the unit at the start of the move.
     */
    public final Point getUnit() {
        return unit;
    }
    
    /**
     * Is set if the unit doesn't attack or move and wants a bonus to defense.
     */
    public final boolean isDefend() {
        return defend;
    }
    
    /**
     * The movement which this unit performs. This can be less than the maximum
     * range of the unit. A unit can make no movement.
     * 
     * @return The array of directions that the unit took (see class doc) or
     * <code>null</code> if no movement is performed.
     */
    public final byte[] getMovement() {
        return movement;
    }
    
    /**
     * The cell in which there is a unit that is being attacked. This position
     * must be under range of attack from the position in which the unit is
     * after the movement has been performed.
     * 
     * The cell position of the unit which is being attacked or
     * <code>null</code> if no unit is attacked.
     */
    public final Point getAttack() {
        return attack;
    }
    
    /**
     * The {@link Scroll} which is being used, if one is used. Only a hero unit
     * can use a scroll. If this scroll has a positional target,
     * @{link {@link #getScrollTarget()} will return it's position.
     * 
     * @return The {@link Scroll} or <code>null</code> if none is used.
     */
    public final Scroll getScroll() {
        return scroll;
    }
    
    /**
     * The target cell or target unit for the {@link Scroll} used, if one is
     * used.
     * 
     * @return The cell position of the target, or <code>null</code> if there's
     * no positional target or no {@link Scroll} is used. If the target is a
     * unit, this returns the cell position of the unit.
     */
    public final Point getScrollTarget() {
        return scrollTarget;
    }
    
    /**
     * The network message from which this was converted, or <code>null</code>
     * if this doesn't originate from the network.
     */
    public final MoveFrom getOrigin() {
        return origin;
    }
    
    /**
     * Converts a {@link MoveFrom} to a {@link Move}. The returned move may be
     * invalid.
     */
    public static Move fromMoveFrom(MoveFrom mesg) {
        Scroll scroll = null;
        
        //TODO: identify item by id & owner. Test if owner had the scroll in inventory...
        
        /*if (mesg.scrollCode != null) {
            ItemType item = ItemType.getInstance(mesg.scrollCode);
            if (item instanceof Scroll) {
                scroll = (Scroll) item;
            }
        }*/
        
        return new Move(
            mesg.unit,
            mesg.defend,
            mesg.movement,
            mesg.attack,
            scroll,
            mesg.scrollTarget,
            mesg
        );
    }

    /**
     * Returns the original network message if there is one, or converts to one
     * otherwise.
     */
    public final MoveFrom toMoveFrom() {
        if (origin != null) {
            return origin;
        }
        
        MoveFrom mesg = new MoveFrom();
        
        mesg.unit = unit;
        mesg.defend = defend;
        mesg.movement = movement;
        mesg.attack = attack;
        mesg.scrollCode = scroll.getClass().getSimpleName();
        mesg.scrollTarget = scrollTarget;
        
        return mesg;
    }
}
