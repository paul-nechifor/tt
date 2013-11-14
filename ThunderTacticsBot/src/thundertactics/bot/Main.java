package thundertactics.bot;

import java.io.IOException;
import java.net.InetAddress;
import thundertactics.exceptions.UnknownMesgEx;

public class Main {
    private static final int MAX = 100000;
    private static final LoopThread LOOP_THREAD = new LoopThread(MAX);
    
    public static void main(String[] args) throws IOException, UnknownMesgEx,
            InterruptedException {
        LOOP_THREAD.start();
        
        for (int i = 0; i < MAX; i++) {
            LOOP_THREAD.addBot(new Bot(
                    InetAddress.getByName("localhost"),
                    8000,
                    "Bot " + Integer.toString(i),
                    500,
                    500,
                    5500,
                    5500));
            Thread.sleep(100);
        }
        Thread.sleep(99999999);
        
        System.in.read();
    }
}
