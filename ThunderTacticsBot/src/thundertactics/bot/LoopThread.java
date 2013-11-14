package thundertactics.bot;

public class LoopThread extends Thread {
    private final Bot[] bots;
    private volatile int current = 0;
    private volatile boolean keepRunning = true;
    
    public LoopThread(int max) {
        bots = new Bot[max];
    }
    
    public synchronized void addBot(Bot b) {
        bots[current] = b;
        current++;
    }

    @Override
    public void run() {
        long lastTime = System.nanoTime();
        long now;
        double delta = 0.0;
        int i;
        
        while (keepRunning) {
            try {
                for (i = 0; i < current; i++) {
                    bots[i].tic(delta);
                }
                Thread.sleep(50);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
            
            now = System.nanoTime();
            delta = (now - lastTime) / 1000000000.0;
            lastTime = now;
        }
    }
    
    public void stopRunning() {
        if (!keepRunning) {
            throw new AssertionError("It wasn't running.");
        }
        
        keepRunning = false;
        this.interrupt();
    }
}
