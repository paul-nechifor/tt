package thundertactics.cfg;

import java.io.File;
import java.io.FileInputStream;
import java.util.Properties;

public class Config {
	private static final Properties PROPERTIES = new Properties();
	private Config() {
	}
	public static void init(){
		init();
	}
	public static void init(String path){
		if(path!=null && !path.endsWith("/")) path +="/";
		if(path==null) path = "";
		try {
			File f = new File(path+"resources/Config.properties");
			PROPERTIES.load(new FileInputStream(f.getAbsoluteFile()));
		} catch (Exception ex) {
			System.err.println("Couldn't read config file\r\nComplete error:");
			ex.printStackTrace();
			System.exit(1);
		}
	}
	public static String get(String property) {
		return PROPERTIES.getProperty(property);
	}
	
	public static int getAsInt(String property) {
	    return Integer.parseInt(PROPERTIES.getProperty(property));
	}
}
