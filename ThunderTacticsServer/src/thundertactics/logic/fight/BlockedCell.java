package thundertactics.logic.fight;

/**
 * An empty class that represents a FightScene cell that is permanently blocked.
 */
public class BlockedCell implements SceneObject {
    public static final BlockedCell INSTANCE = new BlockedCell();
    
    private BlockedCell() {
    }
}
