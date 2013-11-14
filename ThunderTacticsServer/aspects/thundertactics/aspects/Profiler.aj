package thundertactics.aspects;

/*
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import thundertactics.Main;
*/

public aspect Profiler {
/*
    private static SimpleDateFormat DATE = new SimpleDateFormat("HH:mm:ss.SSS");
    private static String OUTPUT = "/home/p/data.html";
    
    private static class Call {
        public long calls = 0;
        public long totalTime = 0;
        public long startCall;
    }
    
    private ConcurrentHashMap<String, HashMap<String, Call>> threads =
            new ConcurrentHashMap<String, HashMap<String, Call>>();
    
    pointcut main() : execution(public static void Main.main(String[]));
    
    before() : main() {
        System.out.println("Started at " + DATE.format(new Date()) + ".");
    }
    
    after() throws IOException : main() {
        System.out.println("Stopped at " + DATE.format(new Date()) + ".");
        
        PrintWriter writer = new PrintWriter(new FileWriter(OUTPUT));
        writer.append(
            "<html><head><script src='sorttable.js'></script>" +
            "<style>" +
                "html {font-family:sans-serif; font-size:11px}" +
                "table {border-collapse:collapse}" +
                "th, td {border:1px solid #000; padding:2px 4px}" +
                "td:nth-child(3), td:nth-child(4) {text-align:right}" +
            "</style>" +
            "</head>" +
            "<body><table class='sortable'>" +
            "<tr><th>Thread</th><th>Method</th><th>Total time</th>" +
                "<th>Calls</th></tr>\n"
        );
        
        String thread;
        String method;
        Call mCall;
        
        for (Entry<String, HashMap<String, Call>> pair : threads.entrySet()) {
            thread = pair.getKey();
            
            for (Entry<String, Call> pair2 : pair.getValue().entrySet()) {
                method = pair2.getKey();
                mCall = pair2.getValue();
                
                writer.format(
                    "<tr><td>%s</td><td>%s</td><td>%d</td><td>%d</td></tr>\n",
                    thread,
                    method,
                    mCall.totalTime,
                    mCall.calls
                );
            }
        }
        
        writer.append("</table></body></html>");
        
        writer.close();
    }

    pointcut all() : call (* * (..)) && !within(Profiler) &&
            !within(MessageStatistics);
    
    before() : all() {
        String name = Thread.currentThread().getName();
        HashMap<String, Call> calls = threads.get(name);
        
        if (calls == null) {
            calls = new HashMap<String, Call>();
        }
        
        threads.put(name, calls);
        
        String sig = thisJoinPoint.getSignature().toString();
        Call mcall = calls.get(sig);
        
        if (mcall == null) {
            mcall = new Call();
            calls.put(sig, mcall);
        }
        
        mcall.calls++;
        mcall.startCall = System.nanoTime();
    }
    
    after() : all() {
        String name = Thread.currentThread().getName();
        HashMap<String, Call> calls = threads.get(name);

        String sig = thisJoinPoint.getSignature().toString();
        Call mcall = calls.get(sig);
        
        mcall.totalTime += System.nanoTime() - mcall.startCall;
    }
*/
}
