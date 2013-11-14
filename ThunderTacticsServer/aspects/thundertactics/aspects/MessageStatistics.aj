package thundertactics.aspects;

/*
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.LinkedList;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import thundertactics.Main;
import thundertactics.comm.mesg.from.MesgFrom;
import thundertactics.comm.web.WebSocket;
*/

public aspect MessageStatistics {
    /*
    private static String OUTPUT = "/home/p/data.csv";
    
    private ConcurrentHashMap<String, LinkedList<Message>> threads =
            new ConcurrentHashMap<String, LinkedList<Message>>();
    
    private static class Message {
        public byte type;
        public long time;
        public boolean from;
        public int length;
    }
    
    pointcut main() : execution(public static void Main.main(String[]));
    pointcut receive() : call(public byte[] WebSocket.read());
    pointcut send(byte[] mesg) : call(public void WebSocket.write(byte[])) &&
            args(mesg);
    
    //pointcut allEx() : handler(*) && !within(MessageStatistics);
    
    after() returning (byte[] mesg) throws IOException: receive() {
        if (mesg == null) {
            return ;
        }
        
        Message m = getMessage();
        m.type = mesg[0];
        m.time = System.nanoTime();
        m.from = true;
        m.length = mesg.length;
    }
    
    before(byte[] mesg) : send(mesg) {
        if (mesg == null) {
            return;
        }
        
        Message m = getMessage();
        m.type = mesg[0];
        m.time = System.nanoTime();
        m.from = false;
        m.length = mesg.length;
    }
    
    after() throws IOException : main() {
        PrintWriter writer = new PrintWriter(new FileWriter(OUTPUT));
        
        String thread;
        for (Entry<String, LinkedList<Message>> pair : threads.entrySet()) {
            thread = pair.getKey();
            for (Message m : pair.getValue()) {
                writer.format("%s,%s,%d,%d\n", thread,
                        MesgFrom.getName(m.type, m.from), m.time, m.length);
            }
        }
        
        writer.close();
    }
    
    private Message getMessage() {
        String name = Thread.currentThread().getName();
        LinkedList<Message> messages = threads.get(name);
        
        if (messages == null) {
            messages = new LinkedList<Message>();
            threads.put(name, messages);
        }
        
        Message m = new Message();
        messages.add(m);
        return m;
    }
    */
}
