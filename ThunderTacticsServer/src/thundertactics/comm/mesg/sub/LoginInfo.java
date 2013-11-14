package thundertactics.comm.mesg.sub;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import thundertactics.logic.OwnableFactory;
import thundertactics.logic.Player;
import thundertactics.logic.Shop;
import thundertactics.logic.fight.Fight;
import thundertactics.logic.fight.FightScene;
import thundertactics.logic.items.ItemType;
import thundertactics.logic.units.UnitType;

public class LoginInfo {
	// TODO: Cache these somehow.
	private static final Map<Long, ItemInfo> ITEM_TYPES = new HashMap<Long, ItemInfo>();
	private static final Map<Long, UnitType> UNIT_TYPES = new HashMap<Long, UnitType>();
	private static final Map<String, FightScene> FIGHT_SCENES = new HashMap<String, FightScene>();
	private static final AutoLoadInfo AUTO_LOAD_INFO = new AutoLoadInfo();
	private static final List<ShopLocationInfo> SHOP_LOCATIONS = new ArrayList<ShopLocationInfo>();
    
    static {
    	//TODO check this later..
        for (Entry<Long, ItemType> e : OwnableFactory.getAllDefaultItems()) {
            ITEM_TYPES.put(e.getKey(), new ItemInfo(e.getValue()));
        }
    
        for (Entry<Long, UnitType> e : OwnableFactory.getAllDefaultUnits()) {
            UNIT_TYPES.put(e.getKey(), e.getValue());
        }

        for (Entry<String, FightScene> e : FightScene.getAll()) {
            FIGHT_SCENES.put(e.getKey(), e.getValue());
        }
    }
    
    public static void loadShops(Collection<Shop> shops) {
        for (Shop s : shops) {
            SHOP_LOCATIONS.add(s.toShopLocation());
        }
    }
    
    public PlayerInfo playerInfo;
    public Map<Long, ItemInfo> itemTypes = ITEM_TYPES;
    public Map<Long, UnitType> unitTypes = UNIT_TYPES;
    public Map<String, FightScene> fightScenes = FIGHT_SCENES;
    public AutoLoadInfo autoLoadInfo = AUTO_LOAD_INFO;
    public int moveTime = Fight.MOVE_TIME;
    public List<ShopLocationInfo> shops = SHOP_LOCATIONS;
    
    public LoginInfo(Player player) {
        playerInfo = player.getInfo();
    }
}