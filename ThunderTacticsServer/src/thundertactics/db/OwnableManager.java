package thundertactics.db;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class OwnableManager {
	public static void saveLastId(long id){
		try{
			Connection c = Database.getConnection();
			PreparedStatement s = c.prepareStatement("update ownable set lastId=?");
			s.setLong(1, id);
			s.execute();
		}catch(Exception ex){
			ex.printStackTrace();
		}
	}
	public static long getLastId(){
		try{
			Connection c = Database.getConnection();
			PreparedStatement s = c.prepareStatement("select * from ownable");
			ResultSet rs = s.executeQuery();
			if (!rs.next()) {
				c.prepareStatement("insert into ownable values(-1)").execute();
				return -1;
			}
			return rs.getLong("lastId");
		}catch(Exception ex){
			ex.printStackTrace();
			return -1;
		}
	}
}
