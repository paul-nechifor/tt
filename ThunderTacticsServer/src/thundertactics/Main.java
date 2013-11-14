package thundertactics;

import java.io.IOException;
import thundertactics.cfg.Config;
import thundertactics.comm.GameServer;

public class Main {
    public static void main(String[] args) throws IOException {
    	Config.init(null);
        GameServer gameServer = new GameServer(Config.getAsInt("port"));
        gameServer.start();
        System.out.println("Press any key to stop the server");
        System.in.read();
        gameServer.stop();
    }
}
