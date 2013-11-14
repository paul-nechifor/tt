package thundertactics.logic.ai;

import java.awt.Point;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import thundertactics.comm.mesg.from.MoveFrom;
import thundertactics.logic.Hero;
import thundertactics.logic.fight.Fight;
import thundertactics.logic.fight.FightLogic;
import thundertactics.logic.fight.FightScene;
import thundertactics.logic.fight.FighterEnd;
import thundertactics.logic.fight.IFighter;
import thundertactics.logic.fight.Move;
import thundertactics.logic.fight.MoveResults;
import thundertactics.logic.fight.TrustedFighter;
import thundertactics.logic.fight.TrustedUnit;
import thundertactics.logic.units.HeroUnit;
import thundertactics.logic.units.Unit;

/**
 * A fighter logic that only does random moves and it will be used just for
 * testing.
 * 
 * @author Paul Nechifor
 */
public class DumbFighter implements IFighter {
	private final Hero hero;
	private Fight mediator;
	private int id;

	public DumbFighter(Hero hero) {
		this.hero = hero;
	}

	@Override
	public Hero getHero() {
		return hero;
	}

	@Override
	public void started(List<IFighter> fighters, FightScene scene,
			Fight mediator) {
		this.mediator = mediator;
		id = -1;
		for (IFighter f : fighters) {
			id++;
			if (f == this)
				break;
		}
		// TODO: The rest.
	}

	@Override
	public void fighterMoved(IFighter fighter, Move move, MoveResults results) {
		// TODO Auto-generated method stub
	}

	private byte[] pathListToBytes(List<MyPoint> path) {
		int pathSize = path.size();
		byte[] result = new byte[pathSize - 1];

		for (int i = 0; i < result.length; i++) {
			MyPoint last = path.get(i);
			MyPoint next = path.get(i + 1);

			int v1 = next.point.x - last.point.x;
			int v2 = next.point.y - last.point.y;
			for (int j = 0; j < FightLogic.movementDirections.length; j += 2) {
				byte[] move = FightLogic.movementDirections[j];
				if (move[0] == v1 && move[1] == v2) {
					result[i] = (byte) j;
					break;
				}
			}
		}
		return result;
	}

	/*
	 * trims a descending path to max+1 points
	 */
	public List<MyPoint> trimPathList(List<MyPoint> points, int maxMoves) {
		int listSize = points.size();
		if (listSize > maxMoves + 1) {
			return points.subList(listSize - maxMoves - 1, listSize);
		}
		return points;
	}

	/*
	 * return path in descending order
	 */
	public List<MyPoint> getPathList(MyPoint point) {
		List<MyPoint> points = new ArrayList<MyPoint>();
		while (point != null) {
			points.add(point);
			point = point.parentPoint;
		}
		return points;
	}

	/*
	 * must be in ascending order
	 */
	public int getFirstAtackIndex(List<MyPoint> points, int atackRange) {
		int size = points.size();
		for (int i = 0; i < size; i++) {
			if (atackRange >= points.get(i).H) {
				return i;
			}
		}
		return -1;
	}

	private MyPoint aStar(Point p1, Point p2) {
		List<MyPoint> openList = new ArrayList<MyPoint>();
		List<MyPoint> closedList = new ArrayList<MyPoint>();

		MyPoint start = new MyPoint(p1);
		start.H = distance(start.point, p2);
		openList.add(start);
		while (!openList.isEmpty()) {

			MyPoint curent = openList.remove(0);
			closedList.add(curent);
			if (MyPoint.myPtEqualPt(curent, p2)) {
				// a path is found don't return the final point
				return curent.parentPoint;
			}
			int cx = curent.point.x, cy = curent.point.y;
			for (int i = 0; i < FightLogic.movementDirections.length; i += 2) {
				byte[] move = FightLogic.movementDirections[i];
				int dx = move[0] + cx, dy = move[1] + cy;
				Point newPt = new Point(dx, dy);
				MyPoint myNewPt = new MyPoint(newPt);

				if (isValidPoint(newPt, p2) && !closedList.contains(myNewPt)) {
					int index = openList.indexOf(myNewPt);
					if (index == -1) {
						myNewPt.setParentAndFGH(curent, p2);
						openList.add(myNewPt);
					} else {
						MyPoint ptInList = openList.get(index);
						int newG = MyPoint.computeG(newPt, curent);
						if (newG < ptInList.G) {
							ptInList.parentPoint = curent;
							ptInList.setG(newG);
						}
					}
				}
			}
			Collections.sort(openList);
		}

		return null;// no path
	}

	private boolean isValidPoint(Point p, Point target) {
		try {
			if (mediator.getFightLogic().getMap()[p.x][p.y] == null
					|| MyPoint.equals(p, target)) {
				return true;
			} else {
				return false;
			}
		} catch (ArrayIndexOutOfBoundsException e) {
			return false;
		}
	}

	private static class MyPoint implements Comparable<MyPoint> {
		Point point;
		MyPoint parentPoint;
		int F;
		int G;
		int H;
		public String toString(){
			return point +" " + F + " " + G +" " + H; 
		}
		public MyPoint(Point point) {
			this.point = point;
		}

		public void setParentAndFGH(MyPoint parentPoint, Point targetPoint) {
			this.parentPoint = parentPoint;
			this.H = distance(point, targetPoint);
			this.G = computeG(point, parentPoint);
			this.F = G + H;
		}

		public static int computeG(Point p1, MyPoint parent) {
			return 1 + parent.G;
		}

		public void setG(int G) {
			this.G = G;
			this.F = G + H;
		}

		@Override
		public boolean equals(Object obj) {
			if (obj != null) {
				if (obj instanceof MyPoint) {
					MyPoint mp = (MyPoint) obj;
					return (this.point.x == mp.point.x && this.point.y == mp.point.y);
				}
			}
			return false;
		}

		public static boolean myPtEqualPt(MyPoint point, Point p) {
			return point.point.x == p.x && point.point.y == p.y;
		}

		public static boolean equals(Point p1, Point p2) {
			return p1.x == p2.x && p1.y == p2.y;
		}

		@Override
		public int compareTo(MyPoint o) {
			if (this.F > o.F) {
				return 1;
			} else if (this.F < o.F) {
				return -1;
			}
			return 0;
		}
	}

	private static int distance(Point p1, Point p2) {
		int dx = p1.x - p2.x;
		int dy = p1.y - p2.y;
		// return (int)Math.ceil(Math.sqrt(dx*dx+dy*dy));
		return Math.abs(dx) + Math.abs(dy);
	}
	
	private MyPoint intermediarPosition;
	
	private Move getMove(Unit attacker, Unit defender, Point attackerLocation,
			Point defenderLocation) {
		MoveFrom moveFrom = new MoveFrom();
		moveFrom.unit = attackerLocation;
		int atackLen = attacker.getTotalAttackRange();
		int maxMoveLen = attacker.getTotalMoveRange();

		if (distance(attackerLocation, defenderLocation) <= atackLen) {
			moveFrom.attack = defenderLocation;
			System.out.println("can atack");
			return Move.fromMoveFrom(moveFrom);
		} else {
			System.out.println("aStar");
			MyPoint mPath = aStar(attackerLocation, defenderLocation);

			if (mPath != null) {
				System.out.println("aStar!=null");
				List<MyPoint> pointList = getPathList(mPath);
				List<MyPoint> trimedList = trimPathList(pointList, maxMoveLen);
				Collections.reverse(trimedList);
				intermediarPosition = trimedList.get(trimedList.size()-1);

				int atackIndex = getFirstAtackIndex(trimedList, atackLen);
				if (atackIndex != -1) {
					// the first position from where he can attack
					trimedList = trimedList.subList(0, atackIndex + 1);
					moveFrom.attack = defenderLocation;
				}

				moveFrom.movement = pathListToBytes(trimedList);

				return Move.fromMoveFrom(moveFrom);
			} else {
				System.out.println("aStar==null");
				return null;
			}
		}
	}

	private Move getMove(TrustedUnit u) {
		//reset it here..
		intermediarPosition = null;
		Point unitLocation = new Point(u.getX(), u.getY());
		List<TrustedFighter> heroes = mediator.getFightLogic()
				.getTrustedFighters();
		byte[]movement = null;
		for (TrustedFighter fighter : heroes) {// for each fighter
			// if(fighter.getFighter() == this) continue;
			if (fighter.getFighter() instanceof DumbFighter)
				continue;
			TrustedUnit heroUnit = fighter.getHero();
			Point heroLocation = new Point(heroUnit.getX(), heroUnit.getY());
			Move move = getMove(u.getUnit(), heroUnit.getUnit(), unitLocation,
					heroLocation);
			if (move != null && move.getAttack()!=null){
				return move;// hurray we attacked the hero
			}
			if(move!=null)
			movement = move.getMovement();
		}
		// well.. not cool we cannot attack a hero unit..
		// keep the movement to hero
		if(intermediarPosition!=null) {
			unitLocation = intermediarPosition.point;
		}
		for (TrustedFighter fighter : heroes) {// for each fighter
			if (fighter.getFighter() instanceof DumbFighter)
				continue;
			List<TrustedUnit> enemyUnits = fighter.getUnits();
			for (TrustedUnit enemy : enemyUnits) { // for each unit of current
													// fighter
				if (enemy.getUnit() instanceof HeroUnit)
					continue;// ignore the hero since we already test it
				Point enemyLocation = new Point(enemy.getX(), enemy.getY());
				if(movement==null){
					Move move = getMove(u.getUnit(), enemy.getUnit(), unitLocation,
							enemyLocation);
					if (move != null)
						return move;// we found an enemy's unit to attack
				}else{
					if(distance(unitLocation, enemyLocation) > u.getUnit().getTotalAttackRange())continue;
					MoveFrom mf = new MoveFrom();
					mf.unit = new Point(u.getX(), u.getY());
					mf.attack = enemyLocation;
					mf.movement = movement;
					return Move.fromMoveFrom(mf);
				}
			}
		}
		if(intermediarPosition!=null && movement!=null){
			MoveFrom mf = new MoveFrom();
			mf.unit = new Point(u.getX(), u.getY());
			mf.movement = movement;
			return Move.fromMoveFrom(mf);
		}
		// that's even worse.. we didn't found anything to attack
		// TODO get a path to move this unit instead of defend each time..
		MoveFrom mf = new MoveFrom();
		mf.unit = new Point(u.getX(), u.getY());
		mf.defend = true;
		return Move.fromMoveFrom(mf);
	}

	@Override
	public void makeMove(int fighter) {
		if (id != fighter)
			return;
		// TODO: Synchronize this somehow. - SOLVED - one move at a time, will
		// be notify after move to move or not the next soldier
		/* sleep just for the sake of animations :D */
		safeSleep(1000);
		TrustedFighter me = mediator.getFightLogic().getTrustedFigter(this);
		for (TrustedUnit unit : me.getUnits()) {
			if (unit.hasMoved())
				continue;
			Move move = getMove(unit);
			mediator.moveMade(this, move);
			// WARNING: Recursive call
			return;
		}
	}

	private void safeSleep(int millis) {
		try {
			Thread.sleep(millis);
		} catch (Exception ex) {
		}
	}

	@Override
	public void fighterSaid(IFighter fighter, String message) {
		// Cannot respond.
	}

	@Override
	public void peaceProposal(IFighter initator) {
		// Always accepts peace.
		mediator.peaceAccepted(this, true);
	}

	@Override
	public void peaceAcceptance(IFighter fighter, boolean accepted) {
		// Doesn't need this info.
	}

	@Override
	public void end(FighterEnd fighterEnd) {
		// TODO Auto-generated method stub
	}

	@Override
	public void goToAfterFight(float x, float y, float rotation) {
		hero.moveToFreeMode(x, y, rotation);
	}
}
