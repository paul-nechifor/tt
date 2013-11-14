package thundertactics.comm.mesg.sub;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import thundertactics.cfg.Config;

/**
 * Lists resources to be autoloaded and their size to display the progress.
 * @author Paul Nechifor
 */
public class AutoLoadInfo {
    public static final String[] TEXTURES = new String[]{
        "textures/texture_diffuse.jpg",
        "textures/water.png",
        "textures/tree1.png",
        "textures/ailanthus_altissima.png",
        "textures/fgrass.png",
        "textures/GRASS3.JPG",
        "textures/shop.jpg",
        "textures/archer.jpg",
        "textures/swordsman1.jpg",
        "textures/razor.jpg"
    };
    
    public static final String[] MODELS = new String[]{
        "models/map.js",
        "models/water.js",
        "models/map_environment.js",
        "models/arena_ground.dae",
        "models/forest.dae",
        "models/characters.dae",
        "models/shop.obj"
    };
    
    public final Map<String, Long> textures = new HashMap<String, Long>();
    public final Map<String, Long> models = new HashMap<String, Long>();
    public long totalSize = 0;
    
    public AutoLoadInfo() {
        File clientPath = new File(Config.get("clientPath"));
        
        for (String texturePath : TEXTURES) {
            File f = new File(clientPath, texturePath);
            long fileSize = f.length();
            textures.put(texturePath, fileSize);
            totalSize += fileSize;
        }
        
        for (String modelPath : MODELS) {
            File f = new File(clientPath, modelPath);
            long fileSize = f.length();
            models.put(modelPath, fileSize);
            totalSize += fileSize;
        }
    }
}
