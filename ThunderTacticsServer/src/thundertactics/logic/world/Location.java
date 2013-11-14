package thundertactics.logic.world;

/**
 * A location represents a point on the whole map measured in meters with
 * both dimensions being positive. It is mutable. Synchronize on the instance
 * when updating the position (it's not thread safe).
 * 
 * @author Paul Nechifor
 */
public class Location {
    public float x;
    public float y;
    
    public float prevX;
    public float prevY;
    
    public boolean moving;
    public float rotation;
    
    public Location(float x, float y) {
        this.x = x;
        this.y = y;
        this.prevX = x;
        this.prevY = y;
    }
    
    public Location(float x, float y, float rotation) {
        this.x = x;
        this.y = y;
        this.prevX = x;
        this.prevY = y;
        this.moving = false;
        this.rotation = rotation;
    }
    
    public Location(Location location) {
        this.x = location.x;
        this.y = location.y;
        this.prevX = location.prevX;
        this.prevY = location.prevY;
        this.moving = location.moving;
        this.rotation = location.rotation;
    }
    public String toString(){
    	return x + " " + y;
    }
}
