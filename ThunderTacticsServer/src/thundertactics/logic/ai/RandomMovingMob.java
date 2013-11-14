package thundertactics.logic.ai;

import thundertactics.comm.GameServer;
import thundertactics.logic.Hero;
import thundertactics.logic.fight.IFighter;
import thundertactics.logic.units.Unit;
import thundertactics.logic.world.MapObserver;
// TODO: Issue a series of commands for a Mob like: move to point P0, wait 40
// seconds, move to P1, move to P2, wait 50s, move to P0.
public class RandomMovingMob extends Mob {
	private final IFighter fighterLogic = new DumbFighter(this);

	public RandomMovingMob(String name, GameServer gameServer, double sx,
			double sy, double r) {
		super(name, gameServer);
		this.setLocation((float) sx, (float) sy, false, 0); 
	}

	@Override
	public void notifyMove(MapObserver observer) {
	}

	@Override
	public void notifyNearChat(MapObserver observer, String text) {
	}

	@Override
	public void notifyAppearanceChange(MapObserver observer) {
	}

	@Override
	public final IFighter getFighterLogic() {
		return fighterLogic;
	}

	@Override
	public void receiveChat(Hero from, String text) {
	}

	@Override
	public void tick(long time) {
	}
	
	/**
	 * Reconstructs the active units slots based on those remaining from the
	 * battlefield.
	 * 
	 * @param if lost remove all, otherwise, remove those with 0 count.
	 */
	public void reconstructUnits(boolean lost) {
		if (lost) {
			this.totalLife = this.totalMaxLife;
			for (int i = 0; i < ACTIVE_UNITS; i++) {
				
				units[i]=new Unit(unitsBackup[i].getType(),unitsBackup[i].getCount());
			}
		} else {
			for (int i = 0; i < ACTIVE_UNITS; i++) {
				if(units[i]==null) continue;
				if(units[i].getCount()<=0) units[i] = null;
			}
		}
	}
	Unit[] unitsBackup;
	@Override
	public void setUnits(Unit[] u){
		super.setUnits(u);
		unitsBackup = new Unit[units.length];
		for (int i = 0; i < ACTIVE_UNITS; i++) {
			unitsBackup[i] = new Unit(units[i].getType(),units[i].getCount());
		}
	}
}
