package thundertactics.comm;

import java.util.List;
import thundertactics.comm.mesg.from.MoveFrom;
import thundertactics.comm.mesg.to.FighterEndTo;
import thundertactics.comm.mesg.to.FighterSaidTo;
import thundertactics.comm.mesg.to.MakeMoveTo;
import thundertactics.comm.mesg.to.MoveAndResultsTo;
import thundertactics.comm.mesg.to.PeaceAcceptanceTo;
import thundertactics.comm.mesg.to.PeaceProposalTo;
import thundertactics.logic.Hero;
import thundertactics.logic.Player;
import thundertactics.logic.fight.Fight;
import thundertactics.logic.fight.FightScene;
import thundertactics.logic.fight.FighterEnd;
import thundertactics.logic.fight.IFighter;
import thundertactics.logic.fight.Move;
import thundertactics.logic.fight.MoveResults;

/**
 * A fighter logic for players. When this fighter receives requests for moves,
 * it forwards them through the WebSocket to the human player. It receives the
 * response and forwards them to the mediator.
 * 
 * @author Paul Nechifor
 */
public class NetworkFighter implements IFighter {
    private final Player player;
    private Fight mediator;
    
    public NetworkFighter(Player player) {
        this.player = player;
    }

    @Override
    public Hero getHero() {
        return player;
    }

    @Override
    public void started(List<IFighter> fighters, FightScene scene,
            Fight mediator) {
        this.mediator = mediator;
        
        player.tryToSend(this.mediator.getFightStartedTo());
    }

    @Override
    public void fighterMoved(IFighter fighter, Move move, MoveResults results) {
        MoveAndResultsTo mesg = new MoveAndResultsTo();
        mesg.move = move.toMoveFrom();
        mesg.results = results.toMoveResultsInfo();
        player.tryToSend(mesg);
    }

    @Override
    public void makeMove(int fighter) {
        MakeMoveTo msg = new MakeMoveTo();
        msg.fighter = fighter;
        player.tryToSend(msg);
    }

    @Override
    public void fighterSaid(IFighter fighter, String message) {
        FighterSaidTo mesg = new FighterSaidTo();
        mesg.from = fighter.getHero().getName();
        mesg.text = message;
        player.tryToSend(mesg);
    }

    @Override
    public void peaceProposal(IFighter initiator) {
    	if(initiator==this) return;
        PeaceProposalTo mesg = new PeaceProposalTo();
        mesg.initiator = initiator.getHero().getName();
        player.tryToSend(mesg);
    }
    
    @Override
    public void peaceAcceptance(IFighter fighter, boolean accepted) {
        PeaceAcceptanceTo mesg = new PeaceAcceptanceTo();
        mesg.name = fighter.getHero().getName();
        mesg.accepted = accepted;
        player.tryToSend(mesg);
    }
    
    public void iMadeThisMove(MoveFrom moveFrom) {
        mediator.moveMade(this, Move.fromMoveFrom(moveFrom));
    }
    
    public void iSaidThis(String text) {
        mediator.say(this, text);
    }
    
    public void iRespondedToPeace(boolean accepted) {
        mediator.peaceAccepted(this, accepted);
    }
    
    public void iProposedPeace() {
        mediator.proposePeace(this);
    }

    @Override
    public void end(FighterEnd fighterEnd) {
    	FighterEndTo fet = fighterEnd.getFighterEndTo();
    	fet.playerInfo = player.getInfo();
    	player.tryToSend(fet);
    }

    @Override
    public void goToAfterFight(float x, float y, float rotation) {
        player.goToAfterFight(x, y, rotation);
    }
}
