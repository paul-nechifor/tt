package thundertactics.aspects;

/*
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.sql.Connection;
import java.text.SimpleDateFormat;
import java.util.Date;

import thundertactics.cfg.Config;
import thundertactics.db.Database;
*/

public aspect SQLExceptionLogger {
/*
	private Object sync = new Object();
    private static SimpleDateFormat DATE = new SimpleDateFormat("HH:mm:ss.SSS");

	after() throwing(Throwable t): execution(Connection Database.getConnection(..)) {
		synchronized (sync) {
			try {
				PrintStream fos = new PrintStream(new FileOutputStream(
						Config.get("logFile"), true));
				fos.println("Error encountered("+DATE.format(new Date())+")");
				t.printStackTrace(fos);
			} catch (Exception ex) {
				System.err.println("Failed to log error - " + t.toString());
			}
		}
	}
*/
}
