package thundertactics.db;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.sql.Blob;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import thundertactics.logic.Player;
import thundertactics.logic.items.ItemType;
import thundertactics.logic.items.WearableItem;
import thundertactics.logic.units.Unit;

public class PlayerManager {

	public static void fillByUsernameAndPassword(Player p) throws Exception {
		Connection c = Database.getConnection();
		PreparedStatement s = c
				.prepareStatement("select * from account where username=? and password=?");
		s.setString(1, p.getName());
		s.setString(2, p.getPasswordHash());
		ResultSet rs = s.executeQuery();
		if (!rs.next())
			throw new Exception("Username/Password invalid");
		p.setId(rs.getInt("id"));
		p.setLevel(rs.getInt("level"));
		p.setExperience(rs.getInt("experience"));
		p.setTotalLife(rs.getInt("life"));
		p.setBaseMaxLife(rs.getInt("max_life"),true);
		p.setGold(rs.getInt("gold"));
		p.setAddPoints(rs.getInt("add_points"));
		p.setBaseDamage(rs.getInt("damage"),true);
		p.setBaseDefense(rs.getInt("defense"),true);
		p.setBaseLeadership(rs.getInt("leadership"),true);
		p.setLocation(rs.getInt("xPos"), rs.getInt("yPos") , false, 0);
		p.setBodyAppearance(rs.getInt("body"));
		p.setClothesAppearance(rs.getInt("clothes"));
		Blob binaryStream = rs.getBlob("inventory");
		ItemType[] inventory = (ItemType[]) getObject(binaryStream);
		p.setInventory(inventory);
		binaryStream = rs.getBlob("weared_items");
		WearableItem[] wearedItems = (WearableItem[]) getObject(binaryStream);
		p.setWornItems(wearedItems);
		binaryStream = rs.getBlob("units");
		Unit[] units = (Unit[]) getObject(binaryStream);
		p.setUnits(units);
		p.equipItems();
		//TODO: load inventory too
		s.close();
	}
	private static Object getObject(Blob blob) throws Exception {
		if(blob==null) return null;
		ObjectInputStream ois = new ObjectInputStream(blob.getBinaryStream());
		Object o = ois.readObject();
		blob.free();
		return o;
	}
	public static void merge(Player p) throws Exception{
		try {
			//TODO: update inventory too
			Connection c = Database.getConnection();
			PreparedStatement s = c
					.prepareStatement("update account set level=?, add_points=?, experience=?, life=?, max_life=?, gold=?, damage=?, defense=?, leadership=?,xPos=?,yPos=?, inventory=?, weared_items=?, units=?  where id=?");

			s.setInt(1, p.getLevel());
			s.setInt(2, p.getAddPoints());
			s.setDouble(3, p.getExperience());
			s.setInt(4, p.getTotalLife());
			s.setInt(5, p.getBaseMaxLife());
			s.setInt(6, p.getGold());
			s.setInt(7, p.getBaseDamage());
			s.setInt(8, p.getBaseDefense());
			s.setInt(9, p.getBaseLeadership());
			s.setFloat(10, p.getLocation().x);
			s.setFloat(11,p.getLocation().y);
			ItemType[] inventory = p.getInventory();
			s.setBlob(12, getInput(inventory));
			WearableItem[] wearedItems = p.getWornItems();
			s.setBlob(13, getInput(wearedItems));
			Unit[] units = p.getUnits();
			s.setBlob(14, getInput(units));
			s.setInt(15, p.getId());
			s.executeUpdate();
			s.close();
		} catch (Exception ex) {
			Exception ex1 = new Exception("Error occurred on updating player: " + ex.getMessage());
			ex1.setStackTrace(ex.getStackTrace());
			throw ex1;
		}
	}
	private static InputStream getInput(Object o) throws Exception{
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		ObjectOutputStream obj = new ObjectOutputStream(baos);
		obj.writeObject(o);
		return new ByteArrayInputStream(baos.toByteArray());
		
	}
	public static void persist(Player p) throws Exception {
		Connection c = Database.getConnection();
		PreparedStatement s = c
				.prepareStatement("select * from account where username=?");
		s.setString(1, p.getName());
		ResultSet rs = s.executeQuery();
		if (rs.next())
			throw new Exception("Username already registered.");
		s.close();
		s = c.prepareStatement("insert into account(username, password, body, clothes) values(?,?,?,?)");
		s.setString(1, p.getName());
		s.setString(2, p.getPasswordHash());
		s.setInt(3, p.getBodyAppearance());
		s.setInt(4, p.getClothesAppearance());
		s.executeUpdate();
		// Avoid using defaults in code and database too..
		fillByUsernameAndPassword(p);
		s.close();
	}
}
