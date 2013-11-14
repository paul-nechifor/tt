package thundertactics.logic;

import java.io.IOException;
import thundertactics.comm.GameServer;
import thundertactics.comm.NetworkFighter;
import thundertactics.comm.mesg.from.TrainFrom;
import thundertactics.comm.mesg.to.ChatTo;
import thundertactics.comm.mesg.to.HeroAppearanceTo;
import thundertactics.comm.mesg.to.LocationTo;
import thundertactics.comm.mesg.to.MesgTo;
import thundertactics.comm.mesg.to.NearChatTo;
import thundertactics.comm.mesg.to.PlayerInfoTo;
import thundertactics.comm.mesg.to.ServerMessageTo;
import thundertactics.comm.mesg.to.TeleportTo;
import thundertactics.comm.web.WebSocket;
import thundertactics.db.PlayerManager;
import thundertactics.logic.fight.IFighter;
import thundertactics.logic.items.ItemType;
import thundertactics.logic.units.Unit;
import thundertactics.logic.units.UnitType;
import thundertactics.logic.world.Location;
import thundertactics.logic.world.MapObserver;
import thundertactics.util.Sha;

/**
 * 
 * @author Paul Nechifor
 */
public class Player extends Hero {
	private transient WebSocket webSocket;
	private transient NetworkFighter fighterLogic;
	private transient boolean loggedIn;
	private transient Location afterFight = null;

	private String passwordHash;

	public Player(String name, GameServer gameServer, WebSocket webSocket,
			String password) {
		super(name, gameServer);
		this.gameServer = gameServer;
		this.webSocket = webSocket;
		this.fighterLogic = new NetworkFighter(this);
		this.loggedIn = true;
		this.passwordHash = Sha.get256Base64(password.getBytes());
	}

	/**
	 * Logs in a player account that has be deserialized and isn't connected to
	 * a WebSocket.
	 */
	public synchronized void login(GameServer gameServer, WebSocket webSocket) {
		this.gameServer = gameServer;
		this.webSocket = webSocket;
		this.fighterLogic = new NetworkFighter(this);
		this.loggedIn = true;
	}
    
    /**
     * Removes the player from the world, sets him as logged off and closes the
     * connection.
     */
	public synchronized void logout(){
		if (!loggedIn) {
			throw new AssertionError("Isn't logged in.");
		}

		exitFreeMode();

		gameServer = null;
		try {
			// Since the message receiver thread listens on the socket, this
			// will throw an exception in that thread, it is handled and the
			// receiver is removed.
			webSocket.close();
		} catch (IOException ex) {
			// Doesn't matter now.
		}
		fighterLogic = null;
		loggedIn = false;
	}

	public synchronized void kick() {
		gameServer.kickPlayer(this);
	}

	/**
	 * Moves a player that is not in free mode to the spawn location.
	 */
	public synchronized void respawn() {
		// Setting previous location to spawn location because enterFreeMode()
		// resets this hero to his previous location.
		Location ownLocation = getLocation();

		if (ownLocation.x != -1 || ownLocation.y != -1) {
			throw new AssertionError("Must not be on map.");
		}

		Location spawn = gameServer.getMap().getSpawnLocation();
		ownLocation.prevX = spawn.x;
		ownLocation.prevY = spawn.y;

		enterFreeMode();
	}

	@Override
	public synchronized IFighter getFighterLogic() {
		return fighterLogic;
	}

	public synchronized boolean isLoggedIn() {
		return loggedIn;
	}

	public synchronized boolean isCorrectPassword(String password) {
		return passwordHash.equals(Sha.get256Base64(password.getBytes()));
	}

	public String getPasswordHash() {
		return passwordHash;
	}

	/**
	 * This is where the validity of the position should be checked.
	 */
	@Override
	public synchronized void setLocation(float x, float y, boolean moving,
			float rotation) {
		// TODO: check if valid.
		super.setLocation(x, y, moving, rotation);
	}

	@Override
	public synchronized void notifyMove(MapObserver observer) {
		Location l = observer.getLocation();

		LocationTo mesg = new LocationTo();
		mesg.i = ((Hero) observer).getId();
		mesg.x = l.x;
		mesg.y = l.y;
		mesg.moving = l.moving;
		mesg.r = l.rotation;

		tryToSend(mesg);
	}

	@Override
	public synchronized void notifyNearChat(MapObserver observer, String text) {
		NearChatTo mesg = new NearChatTo();
		mesg.from = ((Hero) observer).getName();
		mesg.text = text;
		tryToSend(mesg);
	}
	
	@Override
	public synchronized void notifyAppearanceChange(MapObserver observer) {
		HeroAppearanceTo mesg = new HeroAppearanceTo();
		mesg.i = ((Hero) observer).getId();
		mesg.a = ((Hero) observer).getAppearance();
		tryToSend(mesg);
	}

	@Override
	public synchronized void receiveChat(Hero from, String text) {
		ChatTo mesg = new ChatTo();
		mesg.from = from.getName();
		mesg.text = text;
		tryToSend(mesg);
	}

	@Override
	public synchronized void teleportTo(float x, float y, float rotation) {
		super.teleportTo(x, y, rotation);

		TeleportTo msg = new TeleportTo();
		msg.x = x;
		msg.y = y;
		msg.r = rotation;
		tryToSend(msg);
	}

	public synchronized void tryToSend(MesgTo mesg) {
		try {
			mesg.write(webSocket);
		} catch (IOException ex) {
			if (gameServer == null) {
				// TODO: Why is a message being sent to a logged off user?
				ex.printStackTrace();
			}
			gameServer.playerClosedConnection(this);
		}
	}

	public void goToAfterFight(float x, float y, float rotation) {
		afterFight = new Location(x, y, rotation);
	}

	public final Location getAfterFight() {
		Location l = afterFight;
		afterFight = null;
		return l;
	}

	public int getBodyAppearance() {
		return this.body;
	}

	public int getClothesAppearance() {
		return this.clothes;
	}

	/*
	 * Override setters to save when major changes have been made.
	 */
	public void save() {
		try {
			PlayerManager.merge(this);
		} catch (Exception ex) {
		    throw new RuntimeException(ex);
		}
	}

	/**
	 * 
	 * @param mesg
	 *            - message with skills to be trained
	 * @return true if the message was valid, false otherwise
	 */
	public boolean train(TrainFrom mesg) {
		//TODO: if success send a message to user to update stats..
		if (mesg.damage < 0 || mesg.defense < 0 || mesg.leadership < 0
				|| mesg.vitality < 0)
			return false;
		int total = mesg.damage + mesg.defense + mesg.leadership
				+ mesg.vitality;
		if (total > addPoints || total == 0)
			return false;
		this.baseDamage += mesg.damage;
		this.baseDefense += mesg.defense;
		this.baseLeadership += mesg.leadership * 5;
		this.baseMaxLife += mesg.vitality * 10;
		this.totalLife += mesg.vitality * 10;

		this.totalDamage += mesg.damage;
		this.totalDefense += mesg.defense;
		this.totalLeadership += mesg.leadership*5;
		this.totalMaxLife += mesg.vitality * 10;
		addPoints -= total;
		
		return true;
	}

	public int countUnits(){
		int count = 0;
		for(Unit u:units){
			if(u==null) continue;
			count += u.getCount() * u.getType().getLeadershipCost();
		}
		return count;
	}
	public synchronized boolean buyItem(Ownable item, int count) {
		if(item instanceof UnitType){
			if(count<1) return false;
			UnitType ut = (UnitType) item;
			int cost = ut.getHireCost() * count;
			if (this.gold < cost) {
				ServerMessageTo smt = new ServerMessageTo();
				smt.text = "You don't have enough gold.";
				tryToSend(smt);
				return false;
			}
			if(this.countUnits()>=totalLeadership-count * ut.getLeadershipCost()){
				ServerMessageTo smt = new ServerMessageTo();
				smt.text = "You don't have enough leadership.";
				tryToSend(smt);
				return false;
			}
			for(int i=0;i<units.length;i++){
				Unit u = units[i];
				if(u==null){
					units[i] = new Unit(ut, count);
					this.gold -= cost;
					return true;
				}
				if(u.getType().getId() == ut.getId()){
					this.gold -= cost;
					u.setCount(u.getCount()+count);
					return true;
				}
			}
			ServerMessageTo smt = new ServerMessageTo();
			smt.text = "You don't have enough room in your inventory.";
			tryToSend(smt);
			return false;
		}else if(item instanceof ItemType){
			int cost = ((ItemType) item).getDefaultValue();
			if(cost>gold){
				ServerMessageTo smt = new ServerMessageTo();
				smt.text = "You don't have enough gold.";
				tryToSend(smt);
				return false;
			}
			for(int i=0;i<inventory.length;i++){
				if(inventory[i]!=null) continue;
				inventory[i] = ((ItemType) item).getNewInstance();
				gold -= cost;
				return true;
			}
			ServerMessageTo smt = new ServerMessageTo();
			smt.text = "You don't have enough room in your inventory.";
			tryToSend(smt);
			return false;
		}
		return false;
	}

	public boolean sellItem(int id) {
		ItemType item;
		for (int i = 0; i < this.wornItems.length; i++)
			if (this.wornItems[i] != null && this.wornItems[i].getId() == id) {
				item = this.wornItems[i];
				gold += item.getDefaultValue() / 4;
				this.wornItems[i] = null;
				
				return true;
			}
		for (int i = 0; i < this.inventory.length; i++)
			if (this.inventory[i] != null && this.inventory[i].getId() == id) {
				item = this.inventory[i];
				gold += item.getDefaultValue()/4;
				this.inventory[i] = null;
				
				return true;
			}
		return false;
	}

	public boolean moveUnit(int f, int t, int c) {
		if(this.units[f] == null) {
			return false;
		}
		if(this.units[f].getCount()< c) {
			return false;
		}
		if(f<0 || f>=units.length || t<0 || t>=units.length) {
			return false;
		}
		if(this.units[t]==null){
			this.units[t] = new Unit(this.units[f].getType(),c);
		}else{
			if(this.units[t].getType().getId() != this.units[f].getType().getId()) {
				return false;
			}
			this.units[t].setCount(this.units[t].getCount() + c);
		}
		this.units[f].setCount(this.units[f].getCount() - c);
		if(this.units[f].getCount()==0) this.units[f] = null;
        return true;
	}

	public boolean sellUnit(int f, int c) {
		if(this.units[f] == null) return false;
		if(this.units[f].getCount()< c) return false;
		if(f<0 || f>=units.length) return false;
		this.units[f].setCount(this.units[f].getCount() - c);
		gold += this.units[f].getType().getHireCost()*c/4;
		if(this.units[f].getCount()==0) this.units[f] = null;
        return true;
		
	}
	
	public void updateStats(){
		PlayerInfoTo pit = new PlayerInfoTo();
        pit.pi = getInfo();
        tryToSend(pit);
	}
}
