package thundertactics.logic.fight;

import java.awt.Point;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import thundertactics.logic.units.HeroUnit;
import thundertactics.logic.units.Unit;

/**
 * This manages the logic of a fight and verifies if the moves are legal.
 * 
 * @author Paul Nechifor
 */
public class FightLogic {
    private final List<TrustedFighter> fighters =
            new ArrayList<TrustedFighter>();
    private final Map<IFighter, TrustedFighter> trans =
            new HashMap<IFighter, TrustedFighter>();
    private final FightScene scene;
    public static final byte[][] movementDirections = new byte[][] {
            new byte[]{ 0, -1}, // 0 is up
            new byte[]{ 1, -1}, // 1 is up-right
            new byte[]{ 1,  0}, // 2 is right ...
            new byte[]{ 1,  1},
            new byte[]{ 0,  1},
            new byte[]{-1,  1},
            new byte[]{-1,  0},
            new byte[]{-1, -1}
    };
    
    /**
     * Hero's units are represented by Unit, blocked cells are represented by
     * BlockedCell.INSTANCE and free cells are represented by null.
     */
    private final SceneObject[][] map;
    
    /**
     * Index of current fighter.
     */
    private int current = 0;
    
    private String reasonForBeingInvalid;
    private MoveResults resultsForLastMove;
    private List<IFighter> losingFighters = new ArrayList<IFighter>();

    
    /**
     * Sync object
     */
    
    private final Object sync = new Object();
    public FightLogic(List<IFighter> toAdd, FightScene fightScene) {
        scene = fightScene;
        
        map = new SceneObject[scene.getWidth()][scene.getHeight()];
        
        if (toAdd.size() > scene.getMaxFighters()) {
            throw new AssertionError("Not enought spots.");
        }

        addBlockedCellsToScene();
        addFightersToScene(toAdd);
        fighters.get(0).prepareForTurn();
    }
    
    public final boolean isValid(IFighter fighter, Move move) {
        TrustedFighter trusted = trans.get(fighter);
        resultsForLastMove = new MoveResults();
        reasonForBeingInvalid = getReason(trusted, move);
        return reasonForBeingInvalid == null;       
    }
    
    public final IFighter getCurrentFighter() {
        return fighters.get(current).getFighter();
    }
    
    public List<TrustedFighter> getTrustedFighters(){
    	return fighters;
    }
    
    public TrustedFighter getTrustedFigter(IFighter fighter){
    	return trans.get(fighter);
    }

    public void tellCurrentFighterToMove() {
    	synchronized (sync) {
            int index = fighters.get(current).getIndex();
            TrustedFighter c = fighters.get(current);
            for (TrustedFighter f : fighters) {
            	if(c==f) continue;
                f.getFighter().makeMove(index);
            }
            c.getFighter().makeMove(index);
		}
    }
    
    public final String getReasonForBeingInvalid() {
        return reasonForBeingInvalid;
    }
    
    public final MoveResults getResultsForLastMove() {
        return resultsForLastMove;
	}

	public SceneObject[][] getMap() {
		return map;
	}

	public void moveToNextFighter() {
		current = (current + 1) % fighters.size();
		TrustedFighter trustedFighter = fighters.get(current);
		if (!fighters.contains(trustedFighter)) {
			moveToNextFighter();
			return;
		}
		trustedFighter.prepareForTurn();
    }
    
    public final boolean hasMoreMoves() {
        return fighters.get(current).getLeftToMove() > 0;
    }
    
    public void remove(IFighter fighter) {
    	synchronized (sync) {
            // Setting current to the previous, so that when moveToNextFighter() is
            // called, it will be set to the one after the removed one.
            current--;
            
            TrustedFighter trusted = trans.get(fighter);
            fighters.remove(fighter);
            trans.remove(fighter);
            trusted.terminateFighter(map);
		}
    }
    
    public final List<IFighter> getLosingFighters() {
        return losingFighters;
    }
    
    public final void clearLosingFighters() {
        losingFighters.clear();
    }
    
    private void addBlockedCellsToScene() {
        byte[][] initMap = scene.getMap();
        
        for (int i = 0; i < initMap.length; i++) {
            for (int j = 0; j < initMap[i].length; j++) {
                if (initMap[i][j] == FightScene.BLOCKED) {
                    map[i][j] = BlockedCell.INSTANCE;
                }
            }
        }
    }
    
    private void addFightersToScene(List<IFighter> toAdd) {
        byte[][][] pos = scene.getPositions();
        
        for (int i = 0, len = toAdd.size(); i < len; i++) {
            TrustedFighter f = new TrustedFighter(toAdd.get(i), i, pos[i], map);
            fighters.add(f);
            trans.put(toAdd.get(i), f);
        }
    }
    
    private final String getReason(TrustedFighter fighter, Move move) {
        Point start = move.getUnit();
        byte sx = (byte) start.x;
        byte sy = (byte) start.y;
        
        TrustedUnit unit = getUnit(start);
        if (unit == null) {
            return "No unit is performing an action.";
        }
        
        if (unit.hasMoved()) {
            return "Unit has already moved";
        }
        
        if (unit.getOwner() != fighter) {
            return "Unit not owned by fighter.";
        }
        
        byte tx, ty;
        byte[] path = move.getMovement();
        if (path != null) {
            MoveDest dest = verifyMovement(unit, sx, sy, path);
            if (dest.reason != null) {
                return dest.reason;
            }
            tx = dest.x;
            ty = dest.y;
            map[tx][ty] = map[sx][sy];
            map[sx][sy] = null;
            unit.performedMove(tx, ty);
        } else {
            tx = sx;
            ty = sy;
            unit.performedMove();
        }
        
        String reason = verifyAttack(unit, tx, ty, move.getAttack());
        if (reason != null) {
            return reason;
        }
        
        if (path == null && move.getAttack() == null && !move.isDefend()) {
            return "If no movement or attack, then it MUST be defend.";
        }
        
        if (move.isDefend() && (path != null || move.getAttack() != null)) {
            return "Can't defend while moving or attacking.";
        }
        
        // TODO: Account somehow for defense.
        
        fighter.madeValidUnitMove();

        return null;
    }
    
    private final MoveDest verifyMovement(TrustedUnit unit, byte sx, byte sy,
            byte[] path) {
        byte x = sx, y = sy;
        byte[] dir;
        
        if (x < 0 || y < 0 || x >= scene.getWidth() || y >= scene.getHeight()) {
            return new MoveDest("Start point out of bounds.");
        }
        
        if (path.length == 0) {
            return new MoveDest("The directions are empty.");
        }
        
        if (path.length > unit.getUnit().getTotalMoveRange()) {
            return new MoveDest("Moving past range.");
        }
        
        for (byte p : path) {
            if (p < 0 || p >= 8) {
                return new MoveDest("Invalid direction.");
            }
            
            dir = movementDirections[p];
            x += dir[0];
            y += dir[1];
            
            if (map[x][y] != null) {
                return new MoveDest("Intermediary cell is not empty.");
            }
        }
        
        return new MoveDest(x, y);
    }
    
    private final String verifyAttack(TrustedUnit aggressor, byte tx,
            byte ty, Point attack) {
        if (attack == null) {
            return null; // No target, no damages.
        }
        
        TrustedUnit target = getUnit(attack);
        if (target == null) {
            return "No unit is performing an action.";
        }
        Unit targetUnit = target.getUnit();
        Unit aggUnit = aggressor.getUnit();
        TrustedFighter targetOwner = target.getOwner();
        
        if (targetOwner == aggressor.getOwner()) {
            return "Can't attack own units. Sorry.";
        }
        
        int dist = Math.abs(tx - target.getX()) + Math.abs(ty - target.getY());
        if (dist > aggUnit.getTotalAttackRange()) {
            return "Attacking past range.";
        }
        
        int hit = aggUnit.getTotalDamage();
        double diff = 0.05;
        hit = (int)((1.0 + diff + Math.random() * diff) * hit);
        
        int died = targetUnit.addHit(hit);
        resultsForLastMove.addDamage(new MoveResults.Damage(attack, died, hit));
        
        // If unit needs to be removed.
        if (targetUnit.getCount() == 0) {
            targetOwner.removeUnit(target, map);
            if (targetUnit instanceof HeroUnit) {
                losingFighters.add(targetOwner.getFighter());
            }
        }
        
        return null;
    }
    
    private final TrustedUnit getUnit(Point p) {
        SceneObject obj = map[p.x][p.y];
        
        if (obj instanceof TrustedUnit) {
            return (TrustedUnit) obj;
        } else {
            return null;
        }
    }

	public int getSceneWidth() {
		// TODO Auto-generated method stub
		return scene.getWidth();
	}

	public int getSceneHeight() {
		// TODO Auto-generated method stub
		return scene.getHeight();
	}
}

class MoveDest {
    byte x, y;
    String reason;
    
    MoveDest(String reason) {
        this.reason = reason;
    }
    
    MoveDest(byte x, byte y) {
        this.x = x;
        this.y = y;
    }
}
