package thundertactics.logic.world;

import java.awt.Point;
import java.util.ArrayList;
import java.util.List;

/**
 * 
 * @author Paul Nechifor
 */
public class Map implements ObservableMap {
    /**
     * A cell represents the smallest dimension that can be occupied.
     */
    public static final float CELL_SIZE = 10.0f;
    
    /**
     * The number of horizontal/vertical cells that make up a block.
     */
    public static final int CELLS_IN_BLOCK = 64;
    
    public static final float BLOCK_SIZE = CELL_SIZE * CELLS_IN_BLOCK;
    
    public static final float MAX_ATTACK_DISTANCE = 80;

    
    private final int xBlocks = 16;
    private final int yBlocks = 16;
    
    /**
     * This is were all the blocks are in memory. Stored as [x][y].
     */
    private final Block[][] blocks = new Block[xBlocks][yBlocks];
    
    private Location spawnLocation = new Location(1000, 1000);
    private Location respawnLocation = new Location(2000, 2000);
    
    public Map() {
        
        // TODO: Get the real occupied cells.
        List<Point> emptyList = new ArrayList<Point>();
        
        for (int x = 0; x < xBlocks; x++) {
            for (int y = 0; y < yBlocks; y++) {
                blocks[x][y] = new Block(CELLS_IN_BLOCK, emptyList);
            }
        }
        
        for (int x = 0; x < xBlocks; x++) {
            for (int y = 0; y < yBlocks; y++) {
                blocks[x][y].findNeighbors(blocks, xBlocks, yBlocks, x, y);
            }
        }
    }

    /**
     * Returns a clone of the initial spawn location for new players.
     */
    public Location getSpawnLocation() {
        return new Location(spawnLocation);
    }
    
    public Location getRespawnLocation() {
        return new Location(respawnLocation);
    }
    
    public int getXBlocks() {
        return xBlocks;
    }
    
    public int getYBlocks() {
        return yBlocks;
    }

    @Override
    public void addObserver(MapObserver observer) {
        Location l = observer.getLocation();
        
        synchronized (l) {
            int bx = (int) (l.x / Map.BLOCK_SIZE);
            int by = (int) (l.y / Map.BLOCK_SIZE);
            Block b = blocks[bx][by];

            b.addObserver(observer);
            b.notifyMoveSelfAndNeigh(observer);
            b.notifySelfAboutOthers(observer);
        }
    }

    @Override
    public void removeObserver(MapObserver observer) {
        Location l = observer.getLocation();
        
        synchronized (l) {
            int bx = (int) (l.prevX / Map.BLOCK_SIZE);
            int by = (int) (l.prevY / Map.BLOCK_SIZE);
            Block b = blocks[bx][by];

            b.removeObserver(observer);
            b.notifyMoveSelfAndNeigh(observer);
        }
    }
    
    @Override
    public void moved(MapObserver observer) {
        Location l = observer.getLocation();
        Block b = null;
        
        synchronized (l) {
            int oldbx = (int) (l.prevX / Map.BLOCK_SIZE);
            int oldby = (int) (l.prevY / Map.BLOCK_SIZE);
            Block oldb = blocks[oldbx][oldby];

            int newbx = (int) (l.x / Map.BLOCK_SIZE);
            int newby = (int) (l.y / Map.BLOCK_SIZE);
            
            // Check if move to another block is necessary.
            if (oldbx != newbx || oldby != newby) {
                Block newb = blocks[newbx][newby];
                oldb.removeObserver(observer);
                newb.addObserver(observer);
                newb.notifySelfAboutOthers(observer);
            }
            
            l.prevX = l.x;
            l.prevY = l.y;

            b = oldb;
        }

        b.notifyMoveSelfAndNeigh(observer);
    }
    
    @Override
    public void chatNearby(MapObserver observer, String text) {
        Location l = observer.getLocation();
        Block b = null;
        
        synchronized (l) {
            int bx = (int) (l.x / Map.BLOCK_SIZE);
            int by = (int) (l.y / Map.BLOCK_SIZE);
            b = blocks[bx][by];
        }
        
        b.notifyNearChatSelfAndNeigh(observer, text);
    }
    
    @Override
    public void appearanceChanged(MapObserver observer) {
        Location l = observer.getLocation();
        Block b = null;
        
        synchronized (l) {
            int bx = (int) (l.x / Map.BLOCK_SIZE);
            int by = (int) (l.y / Map.BLOCK_SIZE);
            b = blocks[bx][by];
        }
        
        b.notifyAppearanceChangeSelfAndNeigh(observer);
    }
    
    public void makeAwareOfOthers(MapObserver observer) {
        Location l = observer.getLocation();
        Block b = null;
        
        synchronized (l) {
            int bx = (int) (l.x / Map.BLOCK_SIZE);
            int by = (int) (l.y / Map.BLOCK_SIZE);
            b = blocks[bx][by];
        }
        
        b.makeAwareOfOthers(observer);
    }
    
    public List<MapObserver> getNeighborsWithinAttackRange(MapObserver hero) {
        return getBlockFor(hero).getNeighborsCloseTo(hero, MAX_ATTACK_DISTANCE);
    }
    
    private Block getBlockFor(MapObserver observer) {
        Location l = observer.getLocation();
        Block b = null;
        
        synchronized (l) {
            int bx = (int) (l.x / Map.BLOCK_SIZE);
            int by = (int) (l.y / Map.BLOCK_SIZE);
            b = blocks[bx][by];
        }
        
        return b;
    }
}
