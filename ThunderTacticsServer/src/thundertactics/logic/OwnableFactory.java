package thundertactics.logic;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import thundertactics.logic.items.CrownOfLeader;
import thundertactics.logic.items.ItemType;
import thundertactics.logic.items.ScrollOfDecimation;
import thundertactics.logic.items.ScrollOfDivineIntervention;
import thundertactics.logic.units.Archer;
import thundertactics.logic.units.DarkKnight;
import thundertactics.logic.units.Guardsman;
import thundertactics.logic.units.Knight;
import thundertactics.logic.units.LongBowman;
import thundertactics.logic.units.UnitType;

public class OwnableFactory {

	private static final Map<Long, ItemType> ITEM_DEFAULT_INSTANCES = new HashMap<Long, ItemType>();
	private static final Map<Long, UnitType> UNIT_DEFAULT_INSTANCES = new HashMap<Long, UnitType>();
    
    static {		
		ITEM_DEFAULT_INSTANCES.put(CrownOfLeader.DEFAULT_INSTANCE.getId(),
				CrownOfLeader.DEFAULT_INSTANCE);
		ITEM_DEFAULT_INSTANCES.put(ScrollOfDecimation.DEFAULT_INSTANCE.getId(),
				ScrollOfDecimation.DEFAULT_INSTANCE);
		ITEM_DEFAULT_INSTANCES.put(
				ScrollOfDivineIntervention.DEFAULT_INSTANCE.getId(),
				ScrollOfDivineIntervention.DEFAULT_INSTANCE);
		
		
		UNIT_DEFAULT_INSTANCES.put(Guardsman.DEFAULT_INSTANCE.getId(), Guardsman.DEFAULT_INSTANCE);
		UNIT_DEFAULT_INSTANCES.put(Knight.DEFAULT_INSTANCE.getId(), Knight.DEFAULT_INSTANCE);
		UNIT_DEFAULT_INSTANCES.put(DarkKnight.DEFAULT_INSTANCE.getId(), DarkKnight.DEFAULT_INSTANCE);
		UNIT_DEFAULT_INSTANCES.put(Archer.DEFAULT_INSTANCE.getId(), Archer.DEFAULT_INSTANCE);
		UNIT_DEFAULT_INSTANCES.put(LongBowman.DEFAULT_INSTANCE.getId(), LongBowman.DEFAULT_INSTANCE);
    }
    
	public static ItemType getItemDefaultInstance(int code) {
		return ITEM_DEFAULT_INSTANCES.get(code);
	}

	public static Set<Entry<Long, ItemType>> getAllDefaultItems() {
		return ITEM_DEFAULT_INSTANCES.entrySet();
	}
	
	public static UnitType getUnitDefaultInstance(int code) {
		return UNIT_DEFAULT_INSTANCES.get(code);
	}

	public static Set<Entry<Long, UnitType>> getAllDefaultUnits() {
		return UNIT_DEFAULT_INSTANCES.entrySet();
	}
	
	public static Ownable getOwnableInstance(int code){
		if(code<=1000)
		return UNIT_DEFAULT_INSTANCES.get(code).getNewInstance();
		else return ITEM_DEFAULT_INSTANCES.get(code).getNewInstance();
	}
}
