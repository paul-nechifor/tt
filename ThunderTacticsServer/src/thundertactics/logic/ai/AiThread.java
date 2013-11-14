package thundertactics.logic.ai;

import java.util.HashMap;
import java.util.Map;

public class AiThread extends Thread {
    private final Map<String, Mob> mobs = new HashMap<String, Mob>();
    private volatile boolean keepRunning = true;
    
    public AiThread() {
    }
    
    public void addMob(Mob m) {
        synchronized (mobs) {
            mobs.put(m.getName(), m);
        }
    }
    
    public void removeMob(Mob m) {
        synchronized (mobs) {
            mobs.remove(m.getName());
        }
    }

    @Override
    public void run() {
        /*long past = System.currentTimeMillis();
        
        while (keepRunning) {
            try {
                synchronized (mobs) {
                    for (Mob m : mobs.values()) {
                        m.tick(past);
                    }
                }
                Thread.sleep(2000);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
            
            past = System.currentTimeMillis() - past;
        }*/
    }
    
    public void stopRunning() {
        if (!keepRunning) {
            throw new AssertionError("It wasn't running.");
        }
        
        keepRunning = false;
        this.interrupt();
    }
}
