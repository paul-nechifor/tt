package thundertactics.logic.world;

import java.awt.Point;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * A blocks is a group of cells for which a hero can be notified.
 * @author Paul Nechifor
 */
public class Block {
    /**
     * Includes self (1) plus all neighbors (3, 5, or 8).
     */
    private Block[] notificationArea;
    
    private final List<Point> occupiedCells;
    /**
     * This is how we'll check fast if a cell in a block is occupied.
     */
    private final boolean[][] occupied;
    private final Set<MapObserver> observers;
    /**
     * Since the occupied cells don't change, you have to set them all on
     * creation.
     * 
     * @param nCells
     * @param occupiedCells 
     */
    public Block(int nCells, List<Point> occupiedCells) {
        // All set to false by default.
        occupied = new boolean[nCells][nCells];
        this.occupiedCells = occupiedCells;
        
        for (Point p : occupiedCells) {
            occupied[p.x][p.y] = false;
        }
        
        observers = new HashSet<MapObserver>();
    }
    
    /**
     * Tells this block to find all his neighbors in the matrix.
     * @param blocks            The neighbor matrix.
     * @param w                 The width (stored as lines).
     * @param h                 The height (stored as columns).
     * @param x                 Position x of self in matrix.
     * @param y                 Position y of self in matrix.
     */
    public void findNeighbors(Block[][] blocks, int w, int h, int x, int y) {
        List<Block> neigh = new ArrayList<Block>();
        
        for (int dx = -1; dx <= 1; dx++) {
            for (int dy = -1; dy <= 1; dy++) {
                if ((x+dx >= 0) && (x+dx < w) && (y+dy >= 0) && (y+dy < h)) {
                    neigh.add(blocks[x + dx][y + dy]);
                }
            }
        }
        
        notificationArea = neigh.toArray(new Block[0]);
    }
    
    public boolean isOccupied(int x, int y) {
        return occupied[x][y];
    }
    
    public List<Point> getOccupiedCells() {
        return occupiedCells;
    }
    
    public void addObserver(MapObserver observer) {
        synchronized (observers) {
            observers.add(observer);
        }
    }
    
    public void removeObserver(MapObserver observer) {
        synchronized (observers) {
            observers.remove(observer);
        }
    }
    
    public List<MapObserver> getNeighborsCloseTo(MapObserver observer,
            float max) {
        List<MapObserver> ret = new ArrayList<MapObserver>();
        
        Location aLoc = observer.getLocation();
        float ax = aLoc.x;
        float ay = aLoc.y;
        
        Location bLoc;
        float deltaX, deltaY;
        double dist;
        
        for (Block b : notificationArea) {
            synchronized (b.observers) {
                for (MapObserver o : b.observers) {
                    if (observer == o) {
                        continue; // Skip himself.
                    }
                    
                    bLoc = o.getLocation();
                    deltaX = bLoc.x - ax;
                    deltaY = bLoc.y - ay;
                    dist = Math.sqrt(deltaX * deltaX + deltaY * deltaY);
                    
                    if (dist < max) {
                        ret.add(o);
                    }
                }
            }
        }
        
        return ret;
    }
    
    public void notifyNearChatSelfAndNeigh(MapObserver observer, String text) {
        for (Block b : notificationArea) {
            synchronized (b.observers) {
                for (MapObserver o : b.observers) {
                    o.notifyNearChat(observer, text);
                }
            }
        }
    }
    
    public void notifyMoveSelfAndNeigh(MapObserver observer) {
        for (Block b : notificationArea) {
            synchronized (b.observers) {
                for (MapObserver o : b.observers) {
                    if (observer != o) {
                        o.notifyMove(observer);
                    }
                }
            }
        }
    }
    public void notifySelfAboutOthers(MapObserver observer){
    	// TODO: optimize this: send notif only about the cells not in range previously.
        for (Block b : notificationArea) {
            synchronized (b.observers) {
                for (MapObserver o : b.observers) {
                    if (observer != o) {
                    	observer.notifyMove(o);
                    }
                }
            }
        }
    }
    public void makeAwareOfOthers(MapObserver observer) {
        for (Block b : notificationArea) {
            synchronized (b.observers) {
                for (MapObserver o : b.observers) {
                    if (observer != o) {
                        observer.notifyMove(o); // Brackets all the way down. :)
                    }
                }
            }
        }
    }

	public void notifyAppearanceChangeSelfAndNeigh(MapObserver observer) {
        for (Block b : notificationArea) {
            synchronized (b.observers) {
                for (MapObserver o : b.observers) {
                    o.notifyAppearanceChange(observer);
                }
            }
        }
	}
}
