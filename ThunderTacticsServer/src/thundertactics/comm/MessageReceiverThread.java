package thundertactics.comm;

import thundertactics.comm.mesg.from.AttackFrom;
import thundertactics.comm.mesg.from.BuyItemFrom;
import thundertactics.comm.mesg.from.ChatFrom;
import thundertactics.comm.mesg.from.EnteredWorldFrom;
import thundertactics.comm.mesg.from.FighterSaidFrom;
import thundertactics.comm.mesg.from.LocationFrom;
import thundertactics.comm.mesg.from.MesgFrom;
import thundertactics.comm.mesg.from.MoveFrom;
import thundertactics.comm.mesg.from.MoveItemFrom;
import thundertactics.comm.mesg.from.MoveUnitFrom;
import thundertactics.comm.mesg.from.NearChatFrom;
import thundertactics.comm.mesg.from.OtherHeroInfoFrom;
import thundertactics.comm.mesg.from.PeaceAcceptanceFrom;
import thundertactics.comm.mesg.from.PeaceProposalFrom;
import thundertactics.comm.mesg.from.ReadyToEnterFrom;
import thundertactics.comm.mesg.from.SellItemFrom;
import thundertactics.comm.mesg.from.SellUnitFrom;
import thundertactics.comm.mesg.from.ShopPossessionsFrom;
import thundertactics.comm.mesg.from.TrainFrom;
import thundertactics.comm.mesg.to.ServerMessageTo;
import thundertactics.comm.web.WebSocket;
import thundertactics.logic.Hero;
import thundertactics.logic.Ownable;
import thundertactics.logic.Player;
import thundertactics.logic.Shop;
import thundertactics.logic.world.Location;

/**
 * Thread which receives and processes messages from players.
 * @author Paul Nechifor
 */
public class MessageReceiverThread extends Thread {
    private static final int MAX_TRIES = 5;
    private static final long MAX_LOGIN_TIME_NANOS = 60 * 1000000000;
    private final GameServer gameServer;
    private final WebSocket webSocket;
    private final long started;
    private Player player;
    private NetworkFighter networkFighter;
    private volatile boolean keepRunning = true;
    
    public MessageReceiverThread(GameServer gameServer, WebSocket webSocket) {
        this.started = System.nanoTime();
        this.gameServer = gameServer;
        this.webSocket = webSocket;
    }
    
    @Override
    public void run() {
        // Tries up to MAX_TRIES to login the user.
        for (int i = 0; i < MAX_TRIES; i++) {
            try {
                player = gameServer.loginOrRegister(webSocket);
                if (player != null) {
                    break;
                }
            } catch (Exception e) {
                // If there's an exception, don't try again, return early.
                gameServer.receiverThreadEnded(this);
                return;
            }
        }
        
        // Checking if the login failed.
        if (player == null) {
            gameServer.receiverThreadEnded(this);
            return;
        }
        
        MesgFrom mesg;
        
        // Blocking here until the player is ready to enter the world.
        try {
            mesg = MesgFrom.read(webSocket);
            if (mesg == null || ! (mesg instanceof EnteredWorldFrom)) {
                keepRunning = false;
            }
        } catch (Exception ex) {
            keepRunning = false;
        }
        
        // If the player is ready.
        if (keepRunning) {
            player.login(gameServer, webSocket);
            player.enterFreeMode(); // Add the player to free mode.
            gameServer.getMap().makeAwareOfOthers(player);
            gameServer.sendMessageToPlayer(player,
                    gameServer.getWelcomeMessage(), false);
            networkFighter = (NetworkFighter) player.getFighterLogic();
        }
        
        // Keep reading messages until log off.
        while (keepRunning) {
            try {
                mesg = MesgFrom.read(webSocket);
                if (mesg == null) {
                    keepRunning = false;
                } else {
                    processMessage(mesg);
                }
            } catch (Exception ex) {
            	ex.printStackTrace();
                keepRunning = false;
            }
        }
        
        gameServer.playerClosedConnection(player);
        gameServer.receiverThreadEnded(this);
    }
    
    /**
     * Stops this thread.
     * TODO: The AssertionError was thrown once. Investigate the problem.
     */
    public void stopRunning() {
        if (!keepRunning) {
            throw new AssertionError("It wasn't running.");
        }
        
        keepRunning = false;
        this.interrupt();
    }
    
    /**
     * Returns true if this thread has exceeded the time limit to try to log in
     * the player.
     * TODO: Call this from somewhere.
     */
    public boolean logginTimeOver() {
        return player == null &&
                (System.nanoTime() > started + MAX_LOGIN_TIME_NANOS);
    }
    
    private void processMessage(MesgFrom mesg) {
        if (mesg instanceof LocationFrom) {
            LocationFrom m = (LocationFrom) mesg;
            player.setLocation(m.x, m.y, m.moving, m.r);
        } else if (mesg instanceof ChatFrom) {
            ChatFrom m = (ChatFrom) mesg;
            player.sendChat(m.to, m.text);
        } else if (mesg instanceof NearChatFrom) {
            NearChatFrom m = (NearChatFrom) mesg;
            player.sayNearby(m.text);
        } else if (mesg instanceof FighterSaidFrom) {
            FighterSaidFrom m = (FighterSaidFrom) mesg;
            networkFighter.iSaidThis(m.text);
        } else if (mesg instanceof MoveFrom) {
            networkFighter.iMadeThisMove((MoveFrom) mesg);
        } else if (mesg instanceof PeaceAcceptanceFrom) {
            PeaceAcceptanceFrom m = (PeaceAcceptanceFrom) mesg;
            networkFighter.iRespondedToPeace(m.accepted);
        } else if (mesg instanceof OtherHeroInfoFrom) {
            int id = ((OtherHeroInfoFrom) mesg).who;
            Hero hero = gameServer.getHero(id);
            if (hero != null) {
                player.tryToSend(hero.getOtherHeroInfoTo());
            }
        } else if (mesg instanceof AttackFrom) {
        	AttackFrom at = (AttackFrom) mesg;
        	if(at.i!=0)
        		gameServer.heroAttacks(player,at.i);
        	else
        		gameServer.heroAttacks(player);
        } else if (mesg instanceof ReadyToEnterFrom) {
            Location l = player.getAfterFight();
            player.teleportTo(l.x, l.y, l.rotation);
        } else if (mesg instanceof ShopPossessionsFrom) {
            ShopPossessionsFrom m = (ShopPossessionsFrom) mesg;
            Shop shop = gameServer.getShop(m.i);
            if (shop == null) {
                gameServer.kickPlayer(player);
                throw new RuntimeException("No such shop.");
            }
            player.tryToSend(shop.toShopPossessionsTo());
        } else if (mesg instanceof PeaceProposalFrom) {
            networkFighter.iProposedPeace();
        } else if (mesg instanceof TrainFrom) {
            boolean ret = player.train((TrainFrom)mesg);
			if(ret) player.save();
        	player.updateStats();
        }   else if (mesg instanceof MoveItemFrom) {
        	if(!player.isInFreeMode()){
        		ServerMessageTo smt = new ServerMessageTo();
        		smt.text = "You cannot move items while in battle - for now";
        		player.tryToSend(smt);
            	player.updateStats();
        		return;
        	}
			MoveItemFrom mif = (MoveItemFrom) mesg;
			try {
				boolean ret = player.moveItem(mif.i, mif.t, mif.w!=0);
				if(ret) {
					player.save();
					player.playerInfoCache.inventory = null;
					player.playerInfoCache.wornItems = null;
				}
			} catch (Exception ex) {ex.printStackTrace();}
        	player.updateStats();
        } else if (mesg instanceof BuyItemFrom) {
			try {
				BuyItemFrom bif = (BuyItemFrom) mesg;
				Shop shop = gameServer.getShop(bif.s);
				if (shop == null){
					return;
				}
				Ownable item = shop.getItem(bif.i);
				if (item != null) {
					double dx = player.getLocation().x - shop.getLocation().x;
					double dy = player.getLocation().y - shop.getLocation().y;
					double dist = Math.sqrt(dx * dx - dy * dy);
					if (dist < 100) {
						boolean ret = player.buyItem(item, bif.c);
						if (ret) {
							player.save();
							player.playerInfoCache.inventory = null;
							player.playerInfoCache.units = null;
						}
			        	player.updateStats();
					}else{
						ServerMessageTo smt = new ServerMessageTo();
						smt.text = "You are too far way from shop.";
						player.tryToSend(smt);
					}
				}
			} catch (Exception ex) {
				ex.printStackTrace();
			}
        } else if (mesg instanceof SellItemFrom) {
        	SellItemFrom sif = (SellItemFrom) mesg;
			Shop shop = gameServer.getShop(sif.s);
			if (shop == null){
				return;
			}
			double dx = player.getLocation().x - shop.getLocation().x;
			double dy = player.getLocation().y - shop.getLocation().y;
			double dist = Math.sqrt(dx * dx - dy * dy);
			if (dist < 100) {
				boolean ret = player.sellItem(sif.i);
				if (ret) {
					player.save();
					player.playerInfoCache.inventory = null;
				}
			}else{
				ServerMessageTo smt = new ServerMessageTo();
				smt.text = "You are too far way from shop.";
				player.tryToSend(smt);
			}
        	player.updateStats();
        } else if (mesg instanceof MoveUnitFrom) {
        	MoveUnitFrom muf = (MoveUnitFrom) mesg;
        	boolean ret = player.moveUnit(muf.f,muf.t,muf.c);
			if (ret) {
				player.save();
				player.playerInfoCache.units = null;
			}
        	player.updateStats();
        } else if (mesg instanceof SellUnitFrom) {
        	SellUnitFrom muf = (SellUnitFrom) mesg;
        	boolean ret = player.sellUnit(muf.f, muf.c);
			if (ret) {
				player.save();
				player.playerInfoCache.units = null;
			}
        	player.updateStats();
        } else {
            // Unknown message.
            gameServer.kickPlayer(player);
        }
    }
}
