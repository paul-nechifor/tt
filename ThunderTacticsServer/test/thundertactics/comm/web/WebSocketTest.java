package thundertactics.comm.web;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import java.io.IOException;
import java.lang.reflect.Field;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Scanner;
import org.junit.Test;

public class WebSocketTest {
    @Test
    public void testWebSocket() {
        int testPort = 6731;
        byte[] data = "asdf".getBytes();
        
        openClientWhichWrites(testPort, data);
        
        sleep(500);
        
        try {
            Socket socket = new Socket(InetAddress.getLocalHost(), testPort);
            WebSocket webSocket = new WebSocket(socket);
            
            assertFalse(webSocket.isOpen());
            assertTrue(getPrivate(webSocket, "socket") == socket);
        } catch (IOException ex) {
            fail(ex.getMessage());
        }
    }

    @Test
    public void testIsOpen() {
        int testPort = 6742;
        
        String data = "GET /mychat HTTP/1.1\r\n" + 
                "Host: server.example.com\r\n" + 
                "Upgrade: websocket\r\n" + 
                "Connection: Upgrade\r\n" + 
                "Sec-WebSocket-Key: x3JJHMbDL1EzLkh9GBhXDw==\r\n" + 
                "Sec-WebSocket-Protocol: chat\r\n" + 
                "Sec-WebSocket-Version: 13\r\n" + 
                "Origin: http://example.com\r\n\r\n";

        openClientWhichWrites(testPort, data.getBytes());
        
        sleep(500);
        
        try {
            Socket socket = new Socket(InetAddress.getLocalHost(), testPort);
            WebSocket webSocket = new WebSocket(socket);
            webSocket.connect();
            
            assertTrue(webSocket.isOpen());
        } catch (IOException ex) {
            fail(ex.getMessage());
        }
    }

    @Test
    public void testWrite() {
    }

    @Test
    public void testClose() {
    }

    @Test
    public void testRead() {
        int testPort = 6753;
        
        byte[] msg = "Test message bytes.".getBytes();
        
        String data = "GET /mychat HTTP/1.1\r\n" + 
                "Host: server.example.com\r\n" + 
                "Upgrade: websocket\r\n" + 
                "Connection: Upgrade\r\n" + 
                "Sec-WebSocket-Key: x3JJHMbDL1EzLkh9GBhXDw==\r\n" + 
                "Sec-WebSocket-Protocol: chat\r\n" + 
                "Sec-WebSocket-Version: 13\r\n" + 
                "Origin: http://example.com\r\n\r\n";

        openWebSocketWhichWrites(testPort, msg);
        
        sleep(500);
        
        try {
            Socket socket = new Socket(InetAddress.getLocalHost(), testPort);
            socket.getOutputStream().write(data.getBytes());
            socket.getOutputStream().flush();
            
            Scanner s = new Scanner(socket.getInputStream());
            String l;
            while (true) {
                l = s.nextLine().trim();
                
                if (l.isEmpty()) {
                    break;
                }
            }
            
            String readMsg = s.nextLine().substring(2);
            
            assertTrue(readMsg.equals(new String(msg)));
        } catch (IOException ex) {
            fail(ex.getMessage());
        }
    }

    private void openClientWhichWrites(final int port, final byte[] data) {
        Thread thread = new Thread() {
            @Override
            public void run() {
                try {
                    ServerSocket ss = new ServerSocket(port);
                    Socket s = ss.accept();
                    s.getOutputStream().write(data);
                    s.getOutputStream().flush();
                    
                    // Block again.
                    ss.accept();
                } catch (IOException ex) {
                    fail(ex.getMessage());
                }
            }
        };
        
        thread.start();
    }
    
    private void openWebSocketWhichWrites(final int port,
            final byte[] message) {
        Thread thread = new Thread() {
            @Override
            public void run() {
                try {
                    WebSocketServer ss = new WebSocketServer(port);
                    WebSocket s = ss.accept();
                    s.write(message);
                    s.close();
                    ss.accept();
                } catch (IOException ex) {
                    fail(ex.getMessage());
                }
            }
        };
        
        thread.start();
    }
    
    private void sleep(int millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
    
    private Object getPrivate(Object object, String name) {
        try {
            Class<?> c = object.getClass();
            Field f = c.getDeclaredField(name);
            f.setAccessible(true);
            return f.get(object);
        } catch (Exception ex) {
            fail(ex.getMessage());
            return null;
        }
    }
}
