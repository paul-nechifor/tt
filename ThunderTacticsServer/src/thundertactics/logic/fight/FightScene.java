package thundertactics.logic.fight;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import thundertactics.logic.Hero;

/**
 * A class which describes the layout of a battlefield, with free spaces and
 * unit positions etc.
 * 
 * TODO: All these instances should be loaded from a file in the future, and not
 * hard coded here.
 * 
 * @author Paul Nechifor
 */
public class FightScene {
    // Other types of blocks may be added in the future.
    public static final byte FREE = 0;
    public static final byte BLOCKED = 1;
    
    private static final Map<String, FightScene> INSTANCES =
            new HashMap<String, FightScene>();
    private static final String[] SCENE_NAMES;
    
    static {
        // '_' means playable space, '#' means blocked space, '\n' starts a new
        // line and all spaces (' ') are ignored.
        
        INSTANCES.put("plain", new FightScene("plain", "forest", 11, 11,
            "# _ _ _ # # # _ _ _ # \n" +
            "_ _ _ _ _ _ _ _ _ _ _ \n" +
            "_ _ _ _ _ _ _ _ _ _ _ \n" +
            "_ _ _ _ _ _ _ _ _ _ _ \n" +
            "_ _ _ _ _ _ _ _ _ _ _ \n" +
            "_ _ # # # # # # # _ _ \n" +
            "_ _ _ _ _ _ _ _ _ _ _ \n" +
            "_ _ _ _ _ _ _ _ _ _ _ \n" +
            "_ _ _ _ _ _ _ _ _ _ _ \n" +
            "_ _ _ _ _ _ _ _ _ _ _ \n" +
            "# _ _ _ # # # _ _ _ #   ",
            new byte[][] {
                new byte[] { 3, 0,   4, 1,   5, 1,   6, 1,   7, 0},
                new byte[] { 3,10,   4, 9,   5, 9,   6, 9,   7,10},
                new byte[] { 0, 3,   0, 4,   0, 5,   0, 6,   0, 7},
                new byte[] {10, 3,  10, 4,  10, 5,  10, 6,  10, 7}
            }
        ));
        
        INSTANCES.put("gangup",  new FightScene("gangup", "desert", 11, 9,
            "# # # _ _ _ _ _ # # # \n" +
            "# # _ _ _ _ _ _ _ # # \n" +
            "_ _ _ _ _ _ _ _ _ _ _ \n" +
            "_ _ _ _ _ _ _ _ _ _ _ \n" +
            "_ _ _ _ _ _ _ _ _ _ _ \n" +
            "_ _ _ _ _ _ _ _ _ _ _ \n" +
            "_ _ _ _ _ _ _ _ _ _ _ \n" +
            "# # _ _ _ _ _ _ _ # # \n" +
            "# # # _ _ _ _ _ # # #   ",
            new byte[][] {
                new byte[] { 0, 2,   0, 3,   0, 4,   0, 5,   0, 6},
                new byte[] {10, 2,  10, 3,  10, 4,  10, 5,  10, 6},
                new byte[] { 3, 8,   4, 8,   5, 8,   6, 8,   7, 8},
                new byte[] { 3, 0,   4, 0,   5, 0,   6, 0,   7, 0}
            }
        ));
        
        INSTANCES.put("hill", new FightScene("hill", "forest", 9, 9,
            "_ _ _ _ _ _ _ _ _ \n" +
            "_ _ _ _ _ _ _ _ _ \n" +
            "_ _ _ _ _ _ _ _ _ \n" +
            "_ _ _ _ # _ _ _ _ \n" +
            "_ _ _ # # # _ _ _ \n" +
            "_ _ _ _ # _ _ _ _ \n" +
            "_ _ _ _ _ _ _ _ _ \n" +
            "_ _ _ _ _ _ _ _ _ \n" +
            "_ _ _ _ _ _ _ _ _   ",
            new byte[][] {
                new byte[] { 2, 0,   3, 0,   4, 0,   5, 0,   6, 0},
                new byte[] { 2, 8,   3, 8,   4, 8,   5, 8,   6, 8},
                new byte[] { 0, 2,   0, 3,   0, 4,   0, 5,   0, 6},
                new byte[] { 8, 2,   8, 3,   8, 4,   8, 5,   8, 6}
            }
        ));
        
        SCENE_NAMES = new String[INSTANCES.size()];
        int i = 0;
        for (String name : INSTANCES.keySet()) {
            SCENE_NAMES[i] = name;
            i++;
        }
    }
    
    private final String key;
    private final int width;
    private final int height;
    private final String stringMap;
    @SuppressWarnings("unused")
    private final String arenaName;
    private final int maxFighters;
    private final byte[][][] positions; // [hero][unit][x=0/y=1]
    private final transient byte[][] map; 
    
    private FightScene(String key, String arenaName, int width, int height,
            String stringMap, byte[][] positions) {
        this.key = key;
        this.arenaName = arenaName;
        this.width = width;
        this.height = height;
        this.stringMap = stringMap.replace(" ", "");
        this.maxFighters = positions.length;
        
        String[] split = this.stringMap.split("\n");
        map = new byte[this.width][this.height];
        char c;
        
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                c = split[y].charAt(x);
                if (c == '_') {
                    map[x][y] = FREE;
                } else if (c == '#') {
                    map[x][y] = BLOCKED;
                } else {
                    throw new AssertionError();
                }
            }
        }
        
        int nUnits = Hero.ACTIVE_UNITS + 1;
        
        this.positions = new byte[maxFighters][nUnits][2];
        for (int i = 0; i < maxFighters; i++) {
            if (positions[i].length != 2 * nUnits) {
                throw new AssertionError();
            }
            
            for (int j = 0; j < nUnits; j++) {
                this.positions[i][j][0] = positions[i][2 * j];
                this.positions[i][j][1] = positions[i][2 * j + 1];
            }
        }
    }
    
    public static FightScene get(String scene) {
        return INSTANCES.get(scene);
    }
    
    public static Set<Entry<String, FightScene>> getAll() {
        return INSTANCES.entrySet();
    }
    
    public static FightScene getFightSceneFor(Hero initiator,
            List<Hero> heroes) {
        // TODO: Take the initiator's position into account.
        
        String scene = SCENE_NAMES[(int)(Math.random() * SCENE_NAMES.length)];
        return FightScene.get(scene);
    }
    
    public final String getKey() {
        return key;
    }
    
    public final int getWidth() {
        return width;
    }
    
    public final int getHeight() {
        return height;
    }
    
    public final int getMaxFighters() {
        return maxFighters;
    }
    
    public final byte[][][] getPositions() {
        return positions;
    }
    
    public final byte[][] getMap() {
        return map;
    }
}
