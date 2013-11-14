package thundertactics.logic.fight;

import thundertactics.logic.units.Unit;

public class TrustedUnit implements SceneObject {
    private final TrustedFighter owner;
    private final Unit unit;
    private boolean moved;
    private byte x;
    private byte y;
    
    public TrustedUnit(TrustedFighter owner, Unit unit, byte x, byte y) {
        this.owner = owner;
        this.unit = unit;
        this.x = x;
        this.y = y;
    }
    
    public final TrustedFighter getOwner() {
        return owner;
    }
    
    public final Unit getUnit() {
        return unit;
    }
    
    public final void setToMovable() {
        moved = false;
    }
    
    public final void performedMove(byte x, byte y) {
        this.x = x;
        this.y = y;
        moved = true;
    }
    
    public final void performedMove() {
        moved = true;
    }
    
    public final boolean hasMoved() {
        return moved;
    }
    
    public final byte getX() {
        return x;
    }
    
    public final byte getY() {
        return y;
    }
}
