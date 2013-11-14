package thundertactics.bot;

import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.util.Scanner;
import thundertactics.comm.mesg.from.EnteredWorldFrom;
import thundertactics.comm.mesg.from.LocationFrom;
import thundertactics.comm.mesg.from.LoginOrRegisterFrom;
import thundertactics.comm.web.WebSocket;
import thundertactics.exceptions.UnknownMesgEx;

public class Bot {
    private static final int SPEED = 30;
    
    private final WebSocket webSocket;
    private final float startX;
    private final float startY;
    private final float endX;
    private final float endY;
    private final Thread ignoreMessages = new Thread() {
        @Override
        public void run() {
            while (true) {
                try {
                    webSocket.read();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    };
    private float x;
    private float y;
    private float r;
    private float dx;
    private float dy;
    
    private double wait;
    
    public Bot(InetAddress address, int port, String name, float startX,
            float startY, float endX, float endY) throws IOException,
            UnknownMesgEx {
        webSocket = makeConnectedWebSocket(address, port);
        this.startX = startX;
        this.startY = startY;
        this.endX = endX;
        this.endY = endY;
        
        LoginOrRegisterFrom msg = new LoginOrRegisterFrom();
        msg.name = name;
        msg.password = "password";
        msg.register = true;
        
        msg.write(webSocket);
        
        webSocket.read();
        
        EnteredWorldFrom confirm = new EnteredWorldFrom();
        confirm.write(webSocket);
        
        ignoreMessages.start();
        
        x = rand(startX, endX);
        y = rand(startY, endY);
        chooseNewState();
    }
    
    public void tic(double delta) throws IOException {
        wait -= delta;
        double nextX = x + delta * dx;
        double nextY = y + delta * dy;
        boolean chagedOrientation = false;
        
        if (wait > 0) {
            if (nextX <= startX || nextX >= endX) {
                dx = -dx;
                chagedOrientation = true;
            }
            if (nextY <= startY || nextY >= endY) {
                dy = -dy;
                chagedOrientation = true;
            }
        } else {
            chooseNewState();
        }
        
        x += delta * dx;
        y += delta * dy;
        
        if (chagedOrientation) {
            r = (float) Math.atan2(dx, dy);
            sendPosition(true);
        }
    }
    
    private void chooseNewState() throws IOException {
        double dice = Math.random();
        wait = 3;//rand(5, 30);
        
        if (dice < 0.4) {
            dx = 0;
            dy = 0;
            sendPosition(false);
        } else {
            r = (float) (Math.random() * 2 * Math.PI - Math.PI);
            dx = (float) (SPEED * Math.sin(r));
            dy = (float) (SPEED * Math.cos(r));
            sendPosition(true);
        }
    }
    
    private void sendPosition(boolean moving) throws IOException {
        LocationFrom l = new LocationFrom();
        l.moving = moving;
        l.x = x;
        l.y = y;
        l.r = r;
        l.write(webSocket);
    }
    
    private WebSocket makeConnectedWebSocket(InetAddress address, int port)
            throws IOException {
        Socket socket = new Socket(address, port);
        
        String data = "GET /mychat HTTP/1.1\r\n" + 
                "Host: server.example.com\r\n" + 
                "Upgrade: websocket\r\n" + 
                "Connection: Upgrade\r\n" + 
                "Sec-WebSocket-Key: x3JJHMbDL1EzLkh9GBhXDw==\r\n" + 
                "Sec-WebSocket-Protocol: chat\r\n" + 
                "Sec-WebSocket-Version: 13\r\n" + 
                "Origin: http://example.com\r\n\r\n";
        
        socket.getOutputStream().write(data.getBytes());
        socket.getOutputStream().flush();
        
        Scanner s = new Scanner(socket.getInputStream());
        while (!s.nextLine().isEmpty()) {
            // Ignore.
        }
        
        WebSocket ret = new WebSocket(socket);
        ret.clientConnect();
        return ret;
    }
    
    private float rand(float start, float end) {
        return (float) (start + Math.random() * (end - start));
    }
}
