package thundertactics.logic;

import java.util.ArrayList;
import java.util.List;
import thundertactics.comm.mesg.sub.PossessionInfo;
import thundertactics.comm.mesg.sub.ShopLocationInfo;
import thundertactics.comm.mesg.to.ShopPossessionsTo;
import thundertactics.logic.items.ItemType;
import thundertactics.logic.units.UnitType;
import thundertactics.logic.world.Location;

public class Shop {
    private final String name;
	private final Location location;
	private final List<Possession<ItemType>> items = new ArrayList<Possession<ItemType>>();
	private final List<Possession<UnitType>> units = new ArrayList<Possession<UnitType>>();
    private static int lastId = 0;
    private static final Object sync = new Object();
	protected int id;
	
	private ShopPossessionsTo cachePossession;
    public Shop(String name, Location location) {
        this.name = name;
        this.location = location;
        synchronized (sync) {
			this.id = ++lastId;
		}
    }
    
    public final String getName() {
        return name;
    }
    
    public final Location getLocation() {
        return location;
    }
    
    public final List<Possession<ItemType>> getItems() {
        return items;
    }
    
    public final List<Possession<UnitType>> getUnits() {
        return units;
    }
    
    public final ShopLocationInfo toShopLocation() {
        ShopLocationInfo ret = new ShopLocationInfo();
        ret.i = id;
        ret.name = name;
        ret.x = location.x;
        ret.y = location.y;
        ret.rotation = location.rotation;
        return ret;
    }
    
    public synchronized ShopPossessionsTo toShopPossessionsTo() {
    	if(cachePossession!=null) return cachePossession;
        cachePossession = new ShopPossessionsTo();
        cachePossession.i = id;
        cachePossession.name = name;
        
        cachePossession.items = new ArrayList<PossessionInfo>();
        for (Possession<ItemType> it : items) {
        	PossessionInfo possessionInfo = new PossessionInfo();
        	it.fill(possessionInfo);
            cachePossession.items.add(possessionInfo);
        }

        cachePossession.units = new ArrayList<PossessionInfo>();
        for (Possession<UnitType> it : units) {
        	PossessionInfo possessionInfo = new PossessionInfo();
        	it.fill(possessionInfo);
            cachePossession.units.add(possessionInfo);
        }
        
        return cachePossession;
    }

	public Integer getId() {
		return this.id;
	}
	public Ownable getItem(int id){

        for (Possession<ItemType> it : items) {
        	if(it.getOwned().getId() == id) return it.getOwned();
        }
        for (Possession<UnitType> it : units) {
        	if(it.getOwned().getId() == id) return it.getOwned();
        }
		return null;
	}
	public boolean hasPossesion(int id) {
		return getItem(id)!=null;
	}
}
