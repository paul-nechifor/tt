package thundertactics.logic.fight;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import thundertactics.comm.GameServer;
import thundertactics.comm.NetworkFighter;
import thundertactics.comm.mesg.sub.FightOpponentInfo;
import thundertactics.comm.mesg.to.FightStartedTo;
import thundertactics.logic.Hero;
import thundertactics.logic.ai.DumbFighter;
import thundertactics.logic.items.ItemType;
import thundertactics.logic.world.Location;

/**
 * The mediator class for a fight.
 * 
 * @author Paul Nechifor
 */
public class Fight {
    /**
     * The maximum number of seconds allowed for a move.
     */
    public static final int MOVE_TIME = 55;
    
    /**
     * The maximum number of fighters in a single fight.
     */
    public static final int MAX_FIGHTERS = 2;
    
    /**
     * Sync object
     */
    private final Object sync = new Object();
    private final GameServer gameServer;
    private final FightLogic fightLogic;
    private final List<IFighter> fighters;
    private final FightScene scene;
    
    /**
     * Time of last move in nanoseconds. This is used calculate if the fighter
     * has passed the limit.
     */
    private long lastMoveTime;
    public boolean fightEnded = false;
    
    private FightStartedTo fightStartedTo;
    private List<IFighter> peaceAcceptors = null;
    @SuppressWarnings("unused")
    private List<ItemType> loot = new ArrayList<ItemType>();
    /* don't know how you think to use loot*/
    /**
     * Used to keep all loots from fight (maximum 3)
     */
    private List<Loot> loots = new ArrayList<Loot>();
    
    /**
     * Helper class used for ordering the fighters.
     */
    private static final class FighterInitiative
            implements Comparable<FighterInitiative>{
        public IFighter fighter;
        public int totalInitative;
        
        public FighterInitiative(IFighter fighter) {
            this.fighter = fighter;
            this.totalInitative = fighter.getHero().getTotalInitative();
        }
        
        @Override
        public int compareTo(FighterInitiative f) {
            return totalInitative - f.totalInitative;
        }
    }
    /**
     * Helper class to save loot and level from a fighter that lost the fight
     * Save level for later. Used to give[retrieve] experience to remaining fighters
     * @author Tiby
     *
     */
    private static final class Loot{
    	public int gold;
    	public int level;
    	public boolean wasMob;
    }
    /**
     * Creates the fight and starts it.
     */
    public Fight(List<Hero> heroes, FightScene scene, GameServer gameServer) {
        this.gameServer = gameServer;
        
        List<IFighter> fighterList = new ArrayList<IFighter>();
        for (Hero hero : heroes) {
            fighterList.add(hero.getFighterLogic());
        }
        fighters = decideFighterOrder(fighterList);

        this.scene = scene;
        fightLogic = new FightLogic(fighters, this.scene);
	}

	public FightLogic getFightLogic() {
		return fightLogic;
	}
	
    public void startFight() {
        constructFightStartedTo(fighters, scene);
        
        // Tell the fighters that it has started and remove them from free mode.
        for (IFighter f : fighters) {
            f.started(fighters, scene, this);
            f.getHero().exitFreeMode();
        }
        
        destroyFightStartedTo();
        
        fightLogic.tellCurrentFighterToMove();
        lastMoveTime = System.nanoTime();
    }
    
    /**
     * The method which a fighter calls to signal a move.
     * @param fighter
     * @param move
     */
    public void moveMade(IFighter fighter, Move move) {
    	synchronized(sync){
	        if(fightEnded) return;
    		// If this wasn't made by the right fighter, remove him and return.
	        if (fightLogic.getCurrentFighter() != fighter) {
	            kickFighter(fighter, "It was not his turn.");
	            return;
	        }
	        
	        // Check if the move is invalid.
	        if (!fightLogic.isValid(fighter, move)) {
	            kickFighter(fighter, fightLogic.getReasonForBeingInvalid());
	        	return;
	        } else {
	            MoveResults moveResults = fightLogic.getResultsForLastMove();
	            
	            // Send the results to all.
	            for (IFighter f : fighters) {
	                f.fighterMoved(fighter, move, moveResults);
	            }
	        }
	        
	        moveToNextState(false);
    	}
    }
    
    /**
     * The method which a fighter calls to appeal for peace.
     * @param initiator
     */
    public void proposePeace(final IFighter initiator) {
        synchronized(sync){
			// If a peace proposal is already in progress.
			if (peaceAcceptors != null) {
				return;
			}

			peaceAcceptors = new ArrayList<IFighter>();
			peaceAcceptors.add(initiator);

			for (final IFighter f : fighters) {
				new Thread(new Runnable(){
					@Override
					public void run() {
						f.peaceProposal(initiator);
					}
					
				}).start();
			}
        }
    }
    
    /**
     * The method which a fighter calls to signal he agrees or not to peace.
     * @param fighter
     */
    public void peaceAccepted(IFighter fighter, boolean accepted) {
    	synchronized (sync) {
            // If there is no peace proposal in progress.
            if (peaceAcceptors == null) {
                return;
            }
            
            // Tell others about his choice.
            for (IFighter f : fighters) {
                f.peaceAcceptance(fighter, accepted);
            }
            
            if (!accepted) {
                // Peace process is ended because of him.
                peaceAcceptors.clear();
                peaceAcceptors = null;
            } else {
                peaceAcceptors.add(fighter);
                
                // If all have accepted.
                if (peaceAcceptors.size() == fighters.size()) {
                    terminateFight(true);
                }
            }
		}
    }
    
    /**
     * The method which a fighter calls to send a message.
     * @param fighter
     * @param message
     */
    public synchronized void say(IFighter fighter, String message) {
        for (IFighter f : fighters) {
            f.fighterSaid(fighter, message);
        }
    }
    
    /**
     * A method called by an external worker to signal that this should check
     * if the current fighter didn't move on time.
     */
    public synchronized void checkMoveAge() {
        if (lastMoveTime + MOVE_TIME * 1000000000 > System.nanoTime()) {
            kickFighter(fightLogic.getCurrentFighter(), "Exceeded time.");
        }
    }
    
    /**
     * Gets the original message to be sent by a NetworkFighter. This is an
     * optimization so that each NetworkFighter won't have to compute this
     * message by himself.
     */
    public FightStartedTo getFightStartedTo() {
        return fightStartedTo;
    }
    
    /**
     * Returns the fighters ordered by their total initiative.
     * 
     * @author Paul Nechifor
     * @param fighters
     * @return
     */
    private List<IFighter> decideFighterOrder(List<IFighter> fighters) {
        List<FighterInitiative> fil = new ArrayList<FighterInitiative>();
        for (IFighter f : fighters) {
            fil.add(new FighterInitiative(f));
        }
        
        Collections.sort(fil);
        
        List<IFighter> ret = new ArrayList<IFighter>(fil.size());
        for (FighterInitiative fi : fil) {
            ret.add(fi.fighter);
        }
        
        return ret;
    }
    
    private void constructFightStartedTo(List<IFighter> fighters,
            FightScene scene) {
        fightStartedTo = new FightStartedTo();
        fightStartedTo.scene = scene.getKey();
        fightStartedTo.fighters = new FightOpponentInfo[fighters.size()];
        
        for (int i = 0; i < fightStartedTo.fighters.length; i++) {
            fightStartedTo.fighters[i] = fighters.get(i).getHero()
                    .getFightOpponenetInfo();
        }
    }
    
    private void destroyFightStartedTo() {
        fightStartedTo = null;
    }
    private boolean fightEnded(){
    	if(fightEnded) return true;
    	if(fighters.size() <= 1) return true;
    	int count = 0;
    	for(IFighter f: fighters){
    		if(f.getHero().getFighterLogic() instanceof NetworkFighter) count++;
    	}
    	return count==0;
    }
    private void moveToNextState(boolean moveAfterKick) {
        // Check for losers.
        for (IFighter f : fightLogic.getLosingFighters()) {
            endForFighter(new FighterEnd(f, FighterEnd.LOST, null));
        }
        fightLogic.clearLosingFighters();
        
        // If end conditions are met.
        if (fightEnded()) {
            terminateFight(false);
        } else {
            if (moveAfterKick || !fightLogic.hasMoreMoves()) {
                fightLogic.moveToNextFighter();
                fightLogic.tellCurrentFighterToMove();
                lastMoveTime = System.nanoTime();
            }else{
            	if(fightLogic.getCurrentFighter() instanceof DumbFighter) fightLogic.tellCurrentFighterToMove();
            }
        }
    }
    
    private void endForFighter(FighterEnd fighterEnd) {
    	synchronized (sync) {
            IFighter fighter = fighterEnd.getFighter();
            
            for (IFighter f : fighters) {
                f.end(fighterEnd);
            }

            Hero hero = fighter.getHero();
            Location ml;
            
            if (fighterEnd.isPositiveEnd()) {
                Location l = hero.getLocation();
				hero.reconstructUnits(false);
                ml = new Location(l.prevX, l.prevY, l.rotation);
            } else {
                grabLoot(fighter);
                if (!(fighter instanceof DumbFighter)) 
                	ml = gameServer.getMap().getRespawnLocation();
                else ml = hero.getLocation();
                /* destroy units, and remove gold only if it's not Mob and lost the battle */
    			//if (!(fighter instanceof DumbFighter)) {
    				hero.reconstructUnits(true);
    			//}
            }
            fighter.goToAfterFight(ml.x, ml.y, ml.rotation);
            
            fighters.remove(fighter);
            
            if (fighterEnd.getType() != FighterEnd.LOST) {
                fightLogic.remove(fighter);
            }
            hero.save();
		}
    }
    
    private void kickFighter(IFighter fighter, String reason) {
        endForFighter(new FighterEnd(fighter, FighterEnd.KICKED, reason));
        moveToNextState(true);
    }
    
    private void terminateFight(boolean peacefully) {
    	synchronized (sync) {
    		if(fightEnded) return;
        	//TODO update client stats(level, gold) for each Player
        	fightEnded = true;
            giveLoot();
            
            if (peacefully) {
                // Tell all that the fight has ended.
                for (IFighter f : fighters) {
                	final IFighter f1 = f;
                    new Thread(new Runnable(){
						@Override
						public void run() {
		                	endForFighter(new FighterEnd(f1, FighterEnd.PEACE, null));
						}
                    }).start();
                }
            } else {
                endForFighter(new FighterEnd(fighters.get(0), FighterEnd.WON, null));
            }
		}
    }
    
    private void grabLoot(IFighter fighter) {
        // TODO: Check later..
    	Hero hero = fighter.getHero();
    	Loot loot = new Loot();
    	loot.level = hero.getLevel();
		if (!(fighter instanceof DumbFighter)) {
			loot.gold = (int) (hero.getGold() * 0.1); // 10% from the player's gold
			hero.setGold((int) (hero.getGold() * 0.9)); // retrieve 10% from the Player's gold
		} else {
			loot.wasMob = true;
			loot.gold = hero.getGold() * Hero.GOLD_RATE; // get the total amount of gold from Mob
		}
    	loots.add(loot);
    }

    private void giveLoot() {
        // TODO: Save somewhere the initiator of the fight. In order to subtract experience for attacking smaller heroes.
        int totalRemaining = fighters.size();
        if(totalRemaining == 0) return;
        int totalGold = 0;
        for(Loot loot: loots){
        	totalGold += loot.gold;
        }
        int goldForEach = totalGold/totalRemaining;
    	for (IFighter f : fighters) {
    		// Mobs should not grow their gold/experience amount..
    		if(f instanceof DumbFighter) continue;
    		Hero hero = f.getHero();
    		hero.setGold(hero.getGold()+goldForEach);
    		double heroLevel = hero.getLevel();
			for (Loot loot : loots) {
				// TODO if the initiator is the winner and his level is signifiant bigger remove some of his experience
    			if(loot.wasMob){
    				System.out.println("loot: " + loot.level + " hero level " + heroLevel);
    				hero.addExperience((loot.level/heroLevel)*loot.level*Hero.EXPERIENCE_RATE);
    			}
    		}
    		
        }
    }
}
