package thundertactics.comm;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;
import thundertactics.comm.mesg.from.LoginOrRegisterFrom;
import thundertactics.comm.mesg.from.MesgFrom;
import thundertactics.comm.mesg.sub.LoginInfo;
import thundertactics.comm.mesg.to.LoginOrRegisterTo;
import thundertactics.comm.mesg.to.ServerMessageTo;
import thundertactics.comm.web.WebSocket;
import thundertactics.comm.web.WebSocketServer;
import thundertactics.db.OwnableManager;
import thundertactics.db.PlayerManager;
import thundertactics.exceptions.UnknownMesgEx;
import thundertactics.logic.Hero;
import thundertactics.logic.Ownable;
import thundertactics.logic.Player;
import thundertactics.logic.Possession;
import thundertactics.logic.Shop;
import thundertactics.logic.ai.AiThread;
import thundertactics.logic.ai.Mob;
import thundertactics.logic.ai.RandomMovingMob;
import thundertactics.logic.fight.Fight;
import thundertactics.logic.fight.FightScene;
import thundertactics.logic.items.CrownOfLeader;
import thundertactics.logic.items.ItemType;
import thundertactics.logic.items.helmets.VikingHelmet;
import thundertactics.logic.items.potions.PotionOfLife;
import thundertactics.logic.items.shields.BigShield;
import thundertactics.logic.items.shields.SmallShield;
import thundertactics.logic.items.weapons.Scythe;
import thundertactics.logic.items.weapons.SmallBow;
import thundertactics.logic.items.weapons.SmallSword;
import thundertactics.logic.units.Archer;
import thundertactics.logic.units.DarkKnight;
import thundertactics.logic.units.Guardsman;
import thundertactics.logic.units.Knight;
import thundertactics.logic.units.LongBowman;
import thundertactics.logic.units.Unit;
import thundertactics.logic.units.UnitType;
import thundertactics.logic.world.Location;
import thundertactics.logic.world.Map;
import thundertactics.logic.world.MapObserver;
import thundertactics.util.Util;

/**
 * The main class of the server.
 * 
 * @author Paul Nechifor
 * @author Tiberiu Pasat
 */
public class GameServer {
    private final WebSocketServer webSocketServer;
    private final HashMap<Integer, Player> players =
            new HashMap<Integer, Player>();
    private final HashMap<Integer, Hero> heroes =
            new HashMap<Integer, Hero>();
    private final Map map;
    private final List<MessageReceiverThread> messageReceivers =
            new ArrayList<MessageReceiverThread>();
    private final AiThread aiThread = new AiThread();
    private final List<Fight> fights = new ArrayList<Fight>();
    private final HashMap<Integer, Shop> shops = new HashMap<Integer, Shop>();

    private volatile boolean keepRunning;

    /**
     * The thread that waits on the WebSocketServer for connections.
     */
    private final Thread acceptorThread = new Thread() {
        @Override
        public void run() {
            while (keepRunning) {
                try {
                    WebSocket webSocket = webSocketServer.accept();
                    MessageReceiverThread r = new MessageReceiverThread(
                            GameServer.this, webSocket);
                    r.start();

                    synchronized (messageReceivers) {
                        messageReceivers.add(r);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    };

    public GameServer(int port) throws IOException {
        webSocketServer = new WebSocketServer(port);
        long lastId = OwnableManager.getLastId();
        Ownable.setLastId(lastId);
        map = new Map();

        // If I don't write this, the static fields in Item subclasses are
        // created in the wrong order. I don't understand why.
        
        FightScene.getAll();
    }

    public void start() throws IOException {
        keepRunning = true;
        acceptorThread.start();
        
        createShops();
        
        createMobs(2000, 0, 0, 6000);
        
        aiThread.start();
    }
    public void stop(){
        for (MessageReceiverThread t : messageReceivers) {
            t.stopRunning();
        }

        keepRunning = false;
        acceptorThread.interrupt();
        aiThread.stopRunning();
    }
    
    private void createMobs(int count, float startX, float startY, float range) {
        float razaMin = 50;
        float razaMax = 70;
        for (int i = 0; i < count; i++) {
        	Random r = new Random();
        	float xPos = startX + razaMax + (int)(Math.random() * (range-2*razaMax));
        	float yPos = startY + razaMax + (int)(Math.random() * (range-2*razaMax));
        	int level = r.nextInt(10);
        	/*
        	 * Levels of mobs by position on the map.
        	 |__10__|__30__|__70__|
        	 |__20__|__40__|__80__|
        	 |__50__|__60__|__90__|
        	 */
        	if(xPos<2000){
        		if(yPos<2000) level+=1;
        		else if(yPos<4000) level+=10;
        		else level+=40;
        	}else
        	if(xPos<4000){
        		if(yPos<2000) level+=20;
        		else if(yPos<4000) level+=30;
        		else level+=50;
        	}else{
            		if(yPos<2000) level+=60;
            		else if(yPos<4000) level+=70;
            		else level+=80;
            	}
            RandomMovingMob rmm = new RandomMovingMob(
                    "("+i+") - level " + level,
                    this,
                    xPos,
                    yPos,
                    razaMin + (int)(Math.random() * (razaMax-razaMin)));
            rmm.setId(-i-1); // negative id for mobs.
            rmm.setLevel(level);
            rmm.setBaseDamage(rmm.getLevel()*10,true);
            rmm.setBaseDefense(rmm.getLevel()*10,true);
            rmm.setBaseMaxLife(rmm.getLevel() * 100,true);
            rmm.setTotalLife(rmm.getBaseMaxLife());
            rmm.setGold(level);
            Unit[] units = rmm.getUnits();
            units[0] = new Unit(Archer.DEFAULT_INSTANCE, level);
            units[1] = new Unit(Guardsman.DEFAULT_INSTANCE, level);
            units[2] = new Unit(Guardsman.DEFAULT_INSTANCE, level);
            units[3] = new Unit(Archer.DEFAULT_INSTANCE, level);
            rmm.setUnits(units);
            rmm.equipItems();
            startMob(rmm);
        }
    }
    
    private void addUnitToShop(Shop s, UnitType t) {
        s.getUnits().add(new Possession<UnitType>(t, t.getHireCost()));
    }
    
    private void createShops() {
        Shop s = new Shop("Spawn Shop", new Location(1580, 1500));
        addUnitToShop(s, Guardsman.DEFAULT_INSTANCE);
        addUnitToShop(s, Knight.DEFAULT_INSTANCE);
        addUnitToShop(s, DarkKnight.DEFAULT_INSTANCE);
        addUnitToShop(s, Archer.DEFAULT_INSTANCE);
        addUnitToShop(s, LongBowman.DEFAULT_INSTANCE);
        s.getItems().add(new Possession<ItemType>(CrownOfLeader.DEFAULT_INSTANCE, CrownOfLeader.DEFAULT_INSTANCE.getDefaultValue()));
        s.getItems().add(new Possession<ItemType>(SmallSword.DEFAULT_INSTANCE, SmallSword.DEFAULT_INSTANCE.getDefaultValue()));
        s.getItems().add(new Possession<ItemType>(SmallBow.DEFAULT_INSTANCE, SmallBow.DEFAULT_INSTANCE.getDefaultValue()));
        s.getItems().add(new Possession<ItemType>(SmallShield.DEFAULT_INSTANCE, SmallShield.DEFAULT_INSTANCE.getDefaultValue()));
        
        s.getItems().add(new Possession<ItemType>(Scythe.DEFAULT_INSTANCE, Scythe.DEFAULT_INSTANCE.getDefaultValue()));
        s.getItems().add(new Possession<ItemType>(BigShield.DEFAULT_INSTANCE, BigShield.DEFAULT_INSTANCE.getDefaultValue()));
        s.getItems().add(new Possession<ItemType>(VikingHelmet.DEFAULT_INSTANCE, VikingHelmet.DEFAULT_INSTANCE.getDefaultValue()));
        shops.put(s.getId(), s);
        
        
        s = new Shop("Balmora Shop", new Location(1400, 1550));
        s.getItems().add(new Possession<ItemType>(PotionOfLife.DEFAULT_INSTANCE, PotionOfLife.DEFAULT_INSTANCE.getDefaultValue()));
        shops.put(s.getId(), s);
        
        LoginInfo.loadShops(shops.values());
    }
    
    public synchronized Shop getShop(int id) {
        return shops.get(id);
    }
    
    public void startMob(Mob m) {
        heroes.put(m.getId(), m);
        map.addObserver(m);
        aiThread.addMob(m);
    }

    private String getReasonForInvalid(String name,String password){
    	if(name==null || !name.matches("[a-zA-Z]([a-zA-Z0-9-.'])+")) return "Name is invalid.";
        if(password==null || password.length()<6) return "Password must have minimum 6 characters";
        return null;
    }
    /**
     * Logs in or registers a player. Called by the message receiver thread.
     * 
     * @param webSocket
     * @throws IOException
     * @throws UnknownMesgEx
     * @return The logged in player, or null on failure.
     * @author Tiberiu Pasat
     * @author Paul Nechifor
     */
    public Player loginOrRegister(WebSocket webSocket) throws IOException,
            UnknownMesgEx {
        LoginOrRegisterFrom lor = (LoginOrRegisterFrom) MesgFrom
                .read(webSocket);
        LoginOrRegisterTo resp = new LoginOrRegisterTo();
        resp.accepted = false;
        Player player = null;
        String reason = getReasonForInvalid(lor.name, lor.password);
        if(reason==null){
	        if (lor.register) {
	            synchronized (players) {
	                try {
	                    player = new Player(lor.name, this, webSocket,
	                            lor.password);
	                    PlayerManager.persist(player);
	                    resp.accepted = true;
	                    players.put(player.getId(), player);
	                    heroes.put(player.getId(), player);
	                } catch (Exception ex) {
	                    ex.printStackTrace();
	                    resp.message = ex.getMessage();
	                }
	            }
	        } else {
	            synchronized (players) {
	                    player = new Player(lor.name, this, webSocket,
	                            lor.password);
	                    // then fill it from database if exists
	                    try {
	                        PlayerManager.fillByUsernameAndPassword(player);
	                        if(players.containsKey(player.getId())) throw new Exception("Account already logged in");
	                        players.put(player.getId(), player);
	                        heroes.put(player.getId(), player);
	                        resp.accepted = true;
	                    } catch (Exception e) {
	                        // if not exists will be thrown an error so we don't
	                        // accept him
	                        e.printStackTrace();
	                        player = null;
	                        resp.message = e.getMessage();
	                    }
	            }
			}
		} else {
			resp.message = reason;
		}

        if (resp.accepted) {
            resp.info = new LoginInfo(player);
        }

        resp.write(webSocket);

        if (resp.accepted) {
            return player; // Return the player (i.e. success).
        }

        return null; // Return failure.
    }
    
    /**
     * @deprecated & not functional anymore..
     */
    public Player loginOrRegister_legacy(WebSocket webSocket)
            throws IOException, UnknownMesgEx {
        LoginOrRegisterFrom lor = (LoginOrRegisterFrom) MesgFrom
                .read(webSocket);
        LoginOrRegisterTo resp = new LoginOrRegisterTo();
        resp.accepted = false;
        Player player = null;

        if (lor.register) {
            synchronized (players) {
                // Check if the name doesn't already exist.
                if (players.containsKey(lor.name)) {
                    //resp.exists = true;
                } else {
                    resp.accepted = true;
                }

                // Add the player if it's okay.
                if (resp.accepted) {
                    player = new Player(lor.name, this, webSocket,
                            lor.password);
                    if(lor.name.startsWith("Bot")) player.setLocation(new Random().nextInt(6000), new Random().nextInt(6000), false, 0);
                    players.put(player.getId(), player);
                    heroes.put(player.getId(), player);
                }
            }
        } else {
            synchronized (players) {
                // Check if the user exits, if the password is correct and if
                // he isn't already logged in.
                player = players.get(lor.name);
                if (player != null && player.isCorrectPassword(lor.password)
                        && !player.isLoggedIn()) {
                    resp.accepted = true;
                }
            }
        }

        if (resp.accepted) {
            resp.info = new LoginInfo(player);
        }

        resp.write(webSocket);

        if (resp.accepted) {
            return player; // Return the player (i.e. success).
        }

        return null; // Return failure.
    }
    
    /**
     * Called to signal that a player should be kicked out due to any reason.
     * 
     * @param player The player to be kicked.
     */
    public void kickPlayer(Player player) {
        System.out.println("Kicking player: " + player.getName());

        // TODO: The rest.

        // write infos about player in database
        try {
            PlayerManager.merge(player);
        } catch (Exception ex) {
            // TODO: Do something if merging was not successful.
            throw new RuntimeException(ex);
        }
        
        // remove player from logged in players list
        synchronized (players) {
            players.remove(player.getId());
            player.logout();
        }
        
        this.getMap().removeObserver(player);
    }

	public Hero getHero(int id) {
		synchronized (heroes) {
            return heroes.get(id);
		}
	}
	
	public Hero getHero(String name) {
		synchronized (players) {
			for(Hero p: players.values()){
				if(p.getName().equals(name)) return p;
			}
		}
		return null;
	}
	
    public void sendChatToHero(Hero from, Hero to, String text) {
        if (to == null || from == null) {
            return; // No such hero.
        }

        to.receiveChat(from, text);
    }

    public void sendChatToAll(Hero from, String text) {
        List<Hero> heroesClone = null;

        synchronized (heroes) {
            heroesClone = new ArrayList<Hero>(players.values());
        }

        for (Hero h : heroesClone) {
            h.receiveChat(from, text);
        }
    }

    /**
     * Called by the receiver thread to signal that a player closed the
     * connection. Note that this also happens when the player logs off.
     * 
     * @param player
     */
    public void playerClosedConnection(Player player) {
        System.out.println("playerClosedConnection");
        kickPlayer(player);

        // TODO: Check if the player is in a fight and tell the mediator that
        // he left.
    }

    /**
     * Message receiver threads are removed then they call this before exiting.
     * 
     * @param thread
     */
    public void receiverThreadEnded(MessageReceiverThread thread) {
        messageReceivers.remove(thread);
    }

    public Map getMap() {
        return map;
    }
    
    public String getWelcomeMessage() {
        return String.format("Welcome to pre-alfa ThunderTactics game. " +
                "There are %d players logged in.", players.size());
    }
    
    /**
     * TODO: Think about how the heroes should be locked so that they can't be
     * attacked by any other hero while this is being processed.
     */
    public synchronized void heroAttacks(Hero initiator) {
        List<MapObserver> reachable =
                map.getNeighborsWithinAttackRange(initiator);
        
        // Return if there's no one to attack.
        if (reachable.isEmpty()) {
            if (initiator instanceof Player) {
                sendMessageToPlayer((Player) initiator,
                        "There is no one to attack.", true);
            }
            
            return;
        }

        // Converting to list of Hero.
        List<Hero> heroes = new ArrayList<Hero>();
        for (MapObserver o : reachable) {
            heroes.add(o.getHero());
        }
        
        
        // Checking if there are to many fighters.
		int i;
        if (heroes.size() >= Fight.MAX_FIGHTERS) {
            // Sorting by distance.
			Util.sortHeroesByDistance(initiator.getLocation(), heroes);
			while ((i = heroes.size()) >= Fight.MAX_FIGHTERS)
				heroes.remove(i - 1);
            
            // Removing the most distant fighters over maxOpponents.
            /*for (int i = heroes.size() - 1; i > maxOpponents; i--) {
                heroes.remove(i);
            }*/
        }
        
        // Getting the appropriate fight scene.
        FightScene scene = FightScene.getFightSceneFor(initiator, heroes);
        
        // Adding the initiator in the list of fighters.
        heroes.add(initiator);
        
        // Starting the fight.
        Fight fight = new Fight(heroes, scene, this);
        fights.add(fight);
        
        fight.startFight();
    }
    
    public void sendMessageToPlayer(Player p, String text, boolean error) {
        ServerMessageTo mesg = new ServerMessageTo();
        mesg.error = error;
        mesg.text = text;
        p.tryToSend(mesg);
    }

	public void heroAttacks(Player player, int i) {
		Hero player2 = heroes.get(i);
		if(player2==null) return;
        // Converting to list of Hero.
        List<Hero> heroes = new ArrayList<Hero>();
        heroes.add(player2);
        
        // Getting the appropriate fight scene.
        FightScene scene = FightScene.getFightSceneFor(player, heroes);
        
        // Adding the initiator in the list of fighters.
        heroes.add(player);
        Fight fight = new Fight(heroes, scene, this);
        fights.add(fight);
        
        fight.startFight();
	}
}
