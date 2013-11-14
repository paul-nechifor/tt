package thundertactics.logic;

import java.util.LinkedList;
import java.util.Random;

import thundertactics.cfg.Config;
import thundertactics.comm.GameServer;
import thundertactics.comm.mesg.sub.FightOpponentInfo;
import thundertactics.comm.mesg.sub.PlayerInfo;
import thundertactics.comm.mesg.sub.PossessionInfo;
import thundertactics.comm.mesg.sub.UnitInfo;
import thundertactics.comm.mesg.to.LocationTo;
import thundertactics.comm.mesg.to.OtherHeroInfoTo;
import thundertactics.logic.fight.IFighter;
import thundertactics.logic.items.ItemType;
import thundertactics.logic.items.WearableItem;
import thundertactics.logic.items.WearableItem.Target;
import thundertactics.logic.items.potions.Potion;
import thundertactics.logic.units.HeroUnit;
import thundertactics.logic.units.Unit;
import thundertactics.logic.world.Location;
import thundertactics.logic.world.MapObserver;

/**
 * This is the superclass for Mobs (server controlled) and Players (human
 * controlled).
 * 
 * @author Paul Nechifor
 */
public abstract class Hero implements MapObserver {
	public static final int EXPERIENCE_RATE = Config.getAsInt("EXPERIENCE_RATE");
	private static final int POINTS_PER_LEVEL = Config.getAsInt("POINTS_PER_LEVEL");;
	public static final int GOLD_RATE = Config.getAsInt("GOLD_RATE");;
	
	/**
	 * This doesn't include the hero unit.
	 */
	public static final int ACTIVE_UNITS = 4;
	public static final int RESERVE_UNITS = 2;
	public static final int INVENTORY_SIZE = 4 * 4;

	public static final String[] APPEARANCES = new String[] {
			"archer,archer1;arrows;bow", "archer,archer2;arrows;bow",
			"archer,archer4;shield;sword", "archer,archer5;sword",
			"swordman,swordsman1;bow", "swordman,swordsman2;bow",
			"swordman,swordsman3;shield;sword", "swordman,swordsman5;sword" };

	protected transient GameServer gameServer;
	protected int id;
	protected String name;
	protected Unit[] units;
	protected ItemType[] inventory;
	protected WearableItem[] wornItems;
	protected Location location;

	// Appearance details
	protected String appearance;
	protected int body;
	protected int clothes;
	// Stats.
	protected int level;
	protected int addPoints;
	protected int gold;
	protected double experience;
	
	protected int baseDefense;
	protected int baseDamage;
	protected int baseLeadership;
	protected int baseMaxLife;
	// Stats by items
	protected int attackRange = 1;
	protected int totalLife;
	protected int totalDamage;
	protected int totalDefense;
	protected int totalLeadership;
	protected int totalMaxLife;

	/**
	 * Constructs a new hero with default level, gold, etc.
	 */
	public Hero(String name, GameServer gameServer) {
		this.gameServer = gameServer;

		this.name = name;
		this.location = gameServer.getMap().getSpawnLocation();

		Random r = new Random();

		units = new Unit[ACTIVE_UNITS + RESERVE_UNITS];

		inventory = new ItemType[INVENTORY_SIZE];
		wornItems = new WearableItem[WearableItem.Target.TOTAL];
		body = r.nextInt(2);
		clothes = r.nextInt(4) + 1;
		updateAppearance();
	}

	public void enterFreeMode() {
		synchronized (location) {
			location.x = location.prevX;
			location.y = location.prevY;
		}
		gameServer.getMap().addObserver(this);
	}

	public final boolean isInFreeMode() {
		synchronized (location) {
			return location.x >= 0;
		}
	}

	public void moveToFreeMode(float x, float y, float rotation) {
		synchronized (location) {
			location.x = x;
			location.y = y;
			location.prevX = x;
			location.prevY = y;
			location.moving = false;
			location.rotation = rotation;
		}
		gameServer.getMap().addObserver(this);
	}

	public void setLocation(float x, float y, boolean moving, float rotation) {
		synchronized (location) {
			location.x = x>0?x: 1;
			location.y = y>0?y: 1;
			location.moving = moving;
			location.rotation = rotation;
		}

		gameServer.getMap().moved(this);
	}

	public void exitFreeMode() {
		synchronized (location) {
			// The hero's position is saved and his current position is set to
			// (-1, -1) so that we can reuse the same method to tell others that
			// the hero was removed from the map.
			location.prevX = location.x;
			location.prevY = location.y;
			location.x = -1;
			location.y = -1;
			location.moving = false;
		}

		gameServer.getMap().removeObserver(this);
	}

	public void setId(int id) {
		this.id = id;
	}

	public int getId() {
		return id;
	}

	/**
	 * Returns the total initiative of this fighter based on the initiative of
	 * the units and their numbers.
	 */
	public int getTotalInitative() {
		int ret = 0;

		for (int i = 0; i < units.length; i++) {
			if (units[i] != null) {
				ret += units[i].getTotalInitative();
			}
		}

		return ret;
	}

	public abstract IFighter getFighterLogic();

	public String getName() {
		return name;
	}

	public void equipItems() {
		totalDamage = baseDamage;
		totalDefense = baseDefense;
		totalMaxLife = baseMaxLife;
		totalLeadership = baseLeadership;
		for (int i = 0; i < wornItems.length; i++) {
			if (wornItems[i] != null)
				wornItems[i].equip(this);
		}
	}
	private void updateInfo() {
	}

	public void teleportTo(float x, float y, float rotation) {
		// If not in free mode, enter it.
		if (isInFreeMode()) {
			setLocation(x, y, false, rotation);
		} else {
			moveToFreeMode(x, y, rotation);
		}
	}

	public void setAttackRange(int attackRange) {
		if (attackRange < 1)
			this.attackRange = 1;
		else
			this.attackRange = attackRange;
	}

	public void setBodyAppearance(int body) {
		this.body = body;
		updateAppearance();
	}

	public void setClothesAppearance(int clothes) {
		this.clothes = clothes;
		updateAppearance();
	}

	public void updateAppearance() {
		appearance = "";
		String texture = "";
		switch (body) {
		default:
		case 0:
			appearance += "swordman";
			texture = clothes>0?"swordsman":"swordman";
			break;
		case 1:
			appearance += "archer";
			texture = "archer";
			break;
		}
		appearance += "," + texture + (clothes>0?clothes:"");
		for (int i = 0; i < wornItems.length; i++) {
			if (wornItems[i] == null)
				continue;
			WearableItem wi = wornItems[i];
			appearance += ";" + wi.getObject3dAppearance();
		}
		gameServer.getMap().appearanceChanged(this);
		updateInfo();
	}

	public void sayNearby(String text) {
		gameServer.getMap().chatNearby(this, text);
	}

	public final void sendChat(String to, String text) {
		if (to == null) {
			gameServer.sendChatToAll(this, text);
		} else {
			gameServer.sendChatToHero(this, gameServer.getHero(text), text);
		}
	}

	public abstract void receiveChat(Hero from, String text);
	
	public PlayerInfo playerInfoCache= new PlayerInfo();
	public PlayerInfo getInfo() {
		//playerInfoCache = new PlayerInfo();

		playerInfoCache.id = id;
		playerInfoCache.name = name;
		if(playerInfoCache.units == null){
			playerInfoCache.units = new LinkedList<PossessionInfo>();
			for (int i = 0; i < units.length; i++) {
				if (units[i] != null) {
					PossessionInfo pi = new PossessionInfo();
					units[i].getType().fill(pi);
					pi.i = units[i].getType().getId();
					pi.c = units[i].getCount() * units[i].getType().getHireCost()/4;
					pi.a = units[i].getType().getAppearance().split(";")[0].replace(",","");
					pi.p.put("c",Integer.toString(units[i].getCount()));
					pi.p.put("p",Integer.toString(i));
					playerInfoCache.units.add(pi);
				}
			}
		}

		if (playerInfoCache.inventory == null) {
			playerInfoCache.inventory = new LinkedList<PossessionInfo>();
			for (int i = 0; i < inventory.length; i++) {
				if (inventory[i] != null) {
					PossessionInfo pi = new PossessionInfo();
					inventory[i].fill(pi);
					pi.p.put("t",
							((Object[]) this.inventory[i].getTargets())[0]
									.toString());
					pi.p.put("p", Integer.toString(i));
					pi.c = inventory[i].getDefaultValue() / 4;
					playerInfoCache.inventory.add(pi);
				}
			}
		}

		if (playerInfoCache.wornItems == null) {
			playerInfoCache.wornItems = new LinkedList<PossessionInfo>();
			for (int i = 0; i < wornItems.length; i++) {
				if (wornItems[i] != null) {
					PossessionInfo pi = new PossessionInfo();
					wornItems[i].fill(pi);
					pi.p.put("t", this.wornItems[i].getTargets()[0].toString());
					pi.c = wornItems[i].getDefaultValue() / 4;
					playerInfoCache.wornItems.add(pi);
				}
			}
		}
		// The location will be where the hero was previously at.
		playerInfoCache.location = new LocationTo();
		playerInfoCache.location.x = location.prevX;
		playerInfoCache.location.y = location.prevY;
		playerInfoCache.location.moving = location.moving;
		playerInfoCache.location.r = location.rotation;

		playerInfoCache.appearance = appearance;

		playerInfoCache.level = level;
		playerInfoCache.addPoints = addPoints;
		playerInfoCache.experience = experience;
		playerInfoCache.life = totalLife;
		playerInfoCache.maxLife = totalMaxLife;
		playerInfoCache.defense = totalDefense;
		playerInfoCache.damage = totalDamage;
		playerInfoCache.leadership = totalLeadership;
		playerInfoCache.gold = gold;

		return playerInfoCache;
	}
	/* @later..
	public InventoryTo getInventoryTo(){
		InventoryTo it = new InventoryTo();
		it.i = new ItemInfo[inventory.length];
		for(int i=0;i<inventory.length;i++){
			it.i[i] = new ItemInfo(inventory[i]);
		}
		return it;
	}*/
	/**
	 * Gets all the information an opponent will see about this hero.
	 */
	public FightOpponentInfo getFightOpponenetInfo() {
		Unit[] ownBattleUnits = getBattleReadyUnits();

		FightOpponentInfo ret = new FightOpponentInfo();
		ret.name = name;

		ret.battleUnits = new UnitInfo[ownBattleUnits.length];
		for (int i = 0; i < ownBattleUnits.length; i++) {
			if (ownBattleUnits[i] != null) {
				ret.battleUnits[i] = ownBattleUnits[i].getUnitInfo();
			}
		}

		ret.level = level;
		ret.defense = baseDefense;
		ret.damage = baseDamage;
		ret.leadership = baseLeadership;
		ret.gold = gold;
		ret.appearance = appearance;

		return ret;
	}

	public OtherHeroInfoTo getOtherHeroInfoTo() {
		OtherHeroInfoTo ret = new OtherHeroInfoTo();
		ret.i = getId();
		ret.name = name;
		ret.appearance = appearance;
		ret.level = level;

		return ret;
	}

	/**
	 * Returns all the active units in the slots including the hero unit in the
	 * order in which they should be positioned on the battlefield.
	 */
	public Unit[] getBattleReadyUnits() {
		Unit[] ret = new Unit[units.length + 1];
		int middle = (ACTIVE_UNITS + 1) / 2;

		for (int i = 0; i < middle; i++) {
			ret[i] = units[i];
		}

		ret[middle] = new HeroUnit(this);

		for (int i = middle; i < units.length; i++) {
			ret[i + 1] = units[i];
		}

		return ret;
	}

	/**
	 * Reconstructs the active units slots based on those remaining from the
	 * battlefield.
	 * 
	 * @param units
	 *            The list of units. If it is <code>null</code> no active units
	 *            will be left.
	 */
	public void reconstructUnits(boolean lost) {
		if (lost) {
			this.totalLife = this.totalMaxLife;
			for (int i = 0; i < ACTIVE_UNITS; i++) {
				units[i] = null;
			}
		} else {
			for (int i = 0; i < ACTIVE_UNITS; i++) {
				if(units[i]==null) continue;
				if(units[i].getCount()<=0) units[i] = null;
			}
		}
		playerInfoCache.units = null;
		updateStats();
	}

	public int getExpNeededToNextLvl() {
		return (level + 9) * level * level * 10;
	}

	/**
	 * 
	 * @param experience
	 */
	public void addExperience(double experience) {
		int expNeeded = getExpNeededToNextLvl();
		this.experience += experience;
		while (this.experience >= expNeeded) {
			this.experience -= expNeeded;
			this.level++;
			// TODO player has reached the next level. Affect other properties
			// like leadership..
			// FIXME
			double addLife = baseMaxLife * 0.1;
			totalMaxLife += addLife;
			totalLife += addLife;
			baseMaxLife += addLife;
			this.addPoints += Hero.POINTS_PER_LEVEL;
			expNeeded = getExpNeededToNextLvl();
		}
		
	}

	/**
	 * Equip an item Adds it to <code>wornItems</code> list Removes it from
	 * inventory
	 * 
	 * @param item
	 *            to be equipped
	 * @return true if found in inventory false if there is already one
	 *         activated on that slot, or it is not found in inventory
	 */
	public boolean equipItem(WearableItem wi) {
		if(!wi.canEquip(this)) return false;
		int pos = wi.getTargets()[0].ordinal();
		// test if can activate the item
		for(int i=0;i < wornItems.length; i++){
			if(wornItems[i]==null) continue;
			Target[] t1 = wornItems[i].getTargets();
			for(int j=0; j< t1.length;j++){
				Target[] t2 = wi.getTargets();
				for(int k=0;k< t2.length;k++){
					if(t2[k].ordinal() == t1[j].ordinal()) return false;
				}
			}
		}
		// TODO: add requirements on item for use (level, damage, defense, etc)
		if (removeItemFromInventory(wi)) {
			wornItems[pos] = wi;
			wi.equip(this);
			updateAppearance();
			return true;
		}
		return false;
	}

	public boolean unequipItem(WearableItem wi) {
		if (wornItems[wi.getTargets()[0].ordinal()] == wi) { // if item is
																// equipped,
																// unequip it.
			wornItems[wi.getTargets()[0].ordinal()] = null;
			wi.unequip(this);
			updateAppearance();
			return true;
		}
		return false;
	}

	public ItemType getItemById(long id){
		for(int i=0;i<this.wornItems.length;i++)
			if(this.wornItems[i]!=null && this.wornItems[i].getId()==id) return this.wornItems[i];
		for(int i=0;i<this.inventory.length;i++)
			if(this.inventory[i]!=null && this.inventory[i].getId()==id) return this.inventory[i];
		return null;
	}
	public boolean moveItem(long id, int to, boolean weared){
		ItemType item = getItemById(id);
		if(weared && to>-1) return moveItem(item, to, false);
		if(weared && to==-1 && item instanceof WearableItem) return equipItem((WearableItem)item);
		if(weared && to==-1 && item instanceof Potion) return usePotion((Potion)item);
		if(to<0){
			System.out.println("Both negative");
			return false;// return moveItem(item,-1,true); //TODO: drop item when both are less than 0
		}
		//otherwise move from a position to another
		return moveItem(item,to,false);
	}
	private boolean usePotion(Potion item) {
		if(item==null) return false;
		if(item.count<=0) {
			removeItemFromInventory(item);
			return false;
		}
		item.use(this);
		if(item.count<=0) removeItemFromInventory(item);
		
		return true;
		
	}

	/**
	 * 
	 * @param wi
	 * @param location
	 * @return
	 */
	public boolean moveItem(ItemType item, int newPosition, boolean drop) {
		if(item == null) {
			System.out.println("Item null?");
			return false;
		}
		if ((newPosition <0 || newPosition >= INVENTORY_SIZE) && !drop){
			System.out.println("Out of bound");
			return false;
			
		}
		if(inventory[newPosition]!=null) {
			System.out.println("Cell taken");
			return false;
		}
		// TODO find item location first.
		// It might be in wornItems or in inventory. If it is in wornItems
		// remove it, otherwise reposition in inventory.
		boolean found = false;
		if (item instanceof WearableItem) {
			found = unequipItem((WearableItem) item);
		}
		if(!found)
			found = removeItemFromInventory(item);
		
		if (!found){
			System.out.println("Not found");
			return false;
		}
		if (!drop)// TODO: add item to map so it can be picked up by anyone
			inventory[newPosition] = item;
		
		return true;
	}

	/**
	 * Removes an item from inventory
	 * 
	 * @param item
	 * @return true if found, false otherwise.
	 */
	public boolean removeItemFromInventory(ItemType item) {
		for (int i = 0; i < inventory.length; i++) {
			if (inventory[i] == item) {
				inventory[i] = null;
				return true;
			}
		}
		return false;
	}

	// Methods used by items to add their own effects

	public void addDamage(int damage) {
		this.totalDamage += damage;
	}

	public void addDefense(int defense) {
		this.totalDefense += defense;
	}

	public void addLeadership(int leadership) {
		this.totalLeadership += leadership;
	}

	public void addLife(int life) {
		this.totalLife += life;
		if(this.totalLife>this.totalMaxLife) this.totalLife = this.totalMaxLife;
	}

	public void addMaxLife(int maxLife) {
		this.totalMaxLife += maxLife;
	}

	
	// Setters & Getters

	public Hero getHero(){
		return this;
	}
	
	public GameServer getGameServer() {
		return gameServer;
	}

	public void setGameServer(GameServer gameServer) {
		this.gameServer = gameServer;
	}

	public Unit[] getUnits() {
		return units;
	}

	public void setUnits(Unit[] units) {
		if(units==null || units.length!=RESERVE_UNITS+ACTIVE_UNITS) units = new Unit[RESERVE_UNITS+ACTIVE_UNITS];
		this.units = units;
		playerInfoCache.units = null;
	}

	public ItemType[] getInventory() {
		return inventory;
	}
	public void save(){}
	public void setInventory(ItemType[] inventory) {
		if(inventory==null || inventory.length!=INVENTORY_SIZE){
			inventory = new ItemType[INVENTORY_SIZE];
		}
		playerInfoCache.inventory = null;
		this.inventory = inventory;
	}

	public WearableItem[] getWornItems() {
		return wornItems;
	}

	public void setWornItems(WearableItem[] wornItems) {
		if(wornItems==null || wornItems.length!=WearableItem.Target.TOTAL) wornItems = new WearableItem[WearableItem.Target.TOTAL];
		this.wornItems = wornItems;
		playerInfoCache.wornItems = null;
		equipItems();
		updateAppearance();
	}

	public Location getLocation() {
		return location;
	}

	public void setLocation(Location location) {
		this.location = location;
	}

	public String getAppearance() {
		return appearance;
	}

	public void setAppearance(String appearance) {
		this.appearance = appearance;
	}

	public int getBody() {
		return body;
	}

	public void setBody(int body) {
		this.body = body;
	}

	public int getClothes() {
		return clothes;
	}

	public void setClothes(int clothes) {
		if (clothes > 4)
			clothes = 0;
		clothes++;
		this.clothes = clothes;
	}

	public int getGold() {
		return gold;
	}

	public void setGold(int gold) {
		this.gold = gold;
		
	}

	public double getExperience() {
		return experience;
	}

	public void setExperience(double experience) {
		this.experience = experience;
		
	}

	public int getLevel() {
		return level;
	}

	public void setLevel(int level) {
		this.level = level;
		
	}

	public int getAddPoints() {
		return addPoints;
	}

	public void setAddPoints(int addPoints) {
		this.addPoints = addPoints;
		
	}

	public int getBaseDefense() {
		return baseDefense;
	}

	public int getBaseDamage() {
		return baseDamage;
	}

	public int getBaseLeadership() {
		return baseLeadership;
	}

	public int getBaseMaxLife() {
		return baseMaxLife;
	}

	public int getTotalLife() {
		return totalLife;
	}

	public void setTotalLife(int totalLife) {
		this.totalLife = totalLife;
		
	}

	public int getTotalDamage() {
		return totalDamage;
	}

	public void setTotalDamage(int totalDamage) {
		this.totalDamage = totalDamage;
	}

	public int getTotalDefense() {
		return totalDefense;
	}

	public void setTotalDefense(int totalDefense) {
		this.totalDefense = totalDefense;
	}

	public int getTotalLeadership() {
		return totalLeadership;
	}

	public void setTotalLeadership(int totalLeadership) {
		this.totalLeadership = totalLeadership;
	}

	public int getTotalMaxLife() {
		return totalMaxLife;
	}

	public void setTotalMaxLife(int totalMaxLife) {
		this.totalMaxLife = totalMaxLife;
	}

	public int getAttackRange() {
		return attackRange;
	}

	public void setName(String name) {
		this.name = name;
	}


	public void setBaseMaxLife(int baseMaxLife) {
		this.baseMaxLife = baseMaxLife;
		
		equipItems();
	}

	public void setBaseLeadership(int baseLeadership) {
		this.baseLeadership = baseLeadership;
		
		equipItems();
	}

	public void setBaseDamage(int baseDamage) {
		this.baseDamage = baseDamage;
		
		equipItems();
	}

	public void setBaseDefense(int baseDefense) {
		this.baseDefense = baseDefense;
		
		equipItems();
	}
	
	// Set base values without re-equipping items - this require a call to equipItems() after all values ar set..

	public void setBaseMaxLife(int baseMaxLife,boolean noEquip) {
		this.baseMaxLife = baseMaxLife;
	}


	public void setBaseLeadership(int baseLeadership,boolean noEquip) {
		this.baseLeadership = baseLeadership;
	}

	public void setBaseDamage(int baseDamage,boolean noEquip) {
		this.baseDamage = baseDamage;
	}

	public void setBaseDefense(int baseDefense,boolean noEquip) {
		this.baseDefense = baseDefense;
	}

	public void updateStats() {
	}
}
