package thundertactics.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;

import thundertactics.cfg.Config;

public class Database {

	public static int max_connections = 10;
	public static List<Connection> connections = new LinkedList<Connection>();
	public static int lastConnectionSent = 0;
	private static Properties connectionProps = new Properties();
	private static String stringConnection = "jdbc:mysql://127.0.0.1:3306/thundertactics";

	static {
		connectionProps.put("user", Config.get("dbusername"));
		connectionProps.put("password", Config.get("dbpassword"));
		connectionProps.put("autoReconnect", "true");
		try {
			Class.forName("com.mysql.jdbc.Driver");
		} catch (Exception ex) {
			ex.printStackTrace();
			System.exit(1);
		}
	}

	/**
	 * Connection pooling and late init Connections should not be closed after
	 * use
	 * 
	 * @return
	 * @throws SQLException
	 */

	public synchronized static Connection getConnection() throws SQLException {
		if (connections.size() < max_connections) {
			Connection conn;
			conn = DriverManager.getConnection(stringConnection,
					connectionProps);
			connections.add(conn);
			return conn;
		} else {
			lastConnectionSent++;
			lastConnectionSent %= max_connections;
			Connection connection = connections.get(lastConnectionSent);
			if(connection.isClosed()){
				try{
					connection.close();
				}catch(Exception ex){}
				connection = DriverManager.getConnection(stringConnection,
						connectionProps);
				connections.set(lastConnectionSent, connection);
			}
			return connection;
		}
	}

	public static void setMaxConnections(int value) {
		max_connections = value;
	}
}
