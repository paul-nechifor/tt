package thundertactics.logic.ai;

import thundertactics.comm.GameServer;
import thundertactics.logic.Hero;

public abstract class Mob extends Hero {    
    public Mob(String name, GameServer gameServer) {
        super(name, gameServer);
    }
    
    public abstract void tick(long time);
}
