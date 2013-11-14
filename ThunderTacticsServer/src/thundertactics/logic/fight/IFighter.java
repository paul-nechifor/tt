package thundertactics.logic.fight;

import java.util.List;
import thundertactics.logic.Hero;

/**
 * The interface used by the {@link Fight} mediator class.
 * 
 * This is the order: {@link #started(List, FightScene, Fight) started()} is the
 * first method called. One of the fighters receives {@link #makeMove()} he
 * computes his move and signals it with a call to {@link
 * Fight#moveMade(IFighter, Move) Fight.moveMade()} and all of them will receive
 * {@link #fighterMoved(IFighter, Move, MoveResults) fighterMoved()}. Repeat
 * this until fight ends.
 * 
 * TODO: <strong>All of this needs to be updated</strong>.
 * 
 * At any playable point a fighter can send Fight.proposePeace() and all of them
 * receive acceptPeace(). If all of them accept by sending Fight.peaceAccepted()
 * the end conditions are triggered.
 * 
 * At any playable point, if a fighter is left without units, died() is sent
 * to all. If there is only one fighter left, end conditions are triggered.
 * 
 * At any playable point, a fighter call call Fight.say() and all of them
 * receive fighterSaid().
 * 
 * If end conditions are triggered, all players are sent ended() and one
 * winner is picked at random and sent spoils() with all the items of the
 * fighters which have died.
 * 
 * @author Paul Nechifor
 */
public interface IFighter {
    /**
     * Return the hero for this fighter logic.
     */
    public Hero getHero();
    
    /**
     * Announces this fighter that the fight has started and sends the order
     * of the fighters.
     */
    public void started(List<IFighter> fighters, FightScene scene,
            Fight mediator);
    
    /**
     * Announces this fighter that another made a move. Self moves are also
     * sent.
     */
    public void fighterMoved(IFighter fighter, Move move, MoveResults results);
    
    /**
     * Announces the start of a figher's turn.
     */
    public void makeMove(int fighter);
    
    /**
     * One of the fighters said something.
     */
    public void fighterSaid(IFighter fighter, String message);
    
    /**
     * A fighter proposes peace.
     */
    public void peaceProposal(IFighter initator);
    
    /**
     * Signals the response of a fighter to the peace offer.
     */
    public void peaceAcceptance(IFighter fighter, boolean accepted);
    
    /**
     * The fight has ended for the fighter given.
     */
    public void end(FighterEnd fighterEnd);
    
    public void goToAfterFight(float x, float y, float rotation);
}
