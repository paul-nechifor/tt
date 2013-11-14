package thundertactics.logic.items;

/**
 * 
 * @author Paul Nechifor
 */
public abstract class Scroll extends ItemType {
    public static enum Target {
        /**
         * Performs an action on any unit.
         */
        UNIT,
        
        /**
         * Performs action on a cell (maybe neighbors too).
         */
        CELL,
        
        /**
         * Performs action on targets that aren't defined by cell position (like
         * on self, or on all units of a certain type).
         */
        GLOBAL
    };

    protected Target target;
    
    /**
     * TODO: This is incomplete, it will be the method which the items with
     * actions will have to override. The parameters will be the things that can
     * be modified.
     */
    public abstract void performAction();
    
    public Target[] getTargets() {
        return new Target[]{target};
    }
}
